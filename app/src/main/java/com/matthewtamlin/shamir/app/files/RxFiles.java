package com.matthewtamlin.shamir.app.files;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.lang.String.format;

/**
 * Utilities for working with files and directories reactively.
 */
public class RxFiles {
  /**
   * Creates the supplied directory, including any required parent directories. The operation will complete
   * successfully if the supplied directory already exists as a directory, but it will fail if the supplied directory
   * already exists as a file.
   * <p>
   * The returned completable does not operate by default on a particular scheduler.
   *
   * @param directory
   *     the directory to create, not null
   *
   * @return a new completable that performs the operation then completes
   */
  @Nonnull
  public Completable createDirectory(@Nonnull final File directory) {
    checkNotNull(directory, "\'directory\' must not be null.");
    
    return Completable.fromAction(() -> FileUtils.forceMkdir(directory));
  }
  
  /**
   * Creates a new empty file. The operation will fail if the file already exists (either as a file or as a directory),
   * or if the parent directory does not exist.
   * <p>
   * The returned completable does not operate by default on a particular scheduler.
   *
   * @param file
   *     the file to create, not null
   *
   * @return a new completable that performs the operation then completes
   */
  @Nonnull
  public Completable createNewFile(@Nonnull final File file) {
    checkNotNull(file, "\'file\' must not be null.");
    
    final Completable createFile = Single
        .fromCallable(file::createNewFile)
        .flatMapCompletable(createdSuccessfully -> createdSuccessfully ?
            Completable.complete() :
            Completable.error(makeError("Failed to create file", file, "Reason unknown")));
    
    return exists(file)
        .flatMapCompletable(exists -> exists ?
            Completable.error(makeError(
                "Failed to create file",
                file,
                "A file or directory already exists at the location")) :
            exists(file.getParentFile())
                .flatMapCompletable(parentExists -> parentExists ?
                    createFile :
                    Completable.error(makeError("Failed to create file", file, "The parent directory does not exist"))));
  }
  
  /**
   * Lists the files in the supplied directory. The operation will fail if the directory does not exist, cannot be
   * accessed, or is actually a file.
   * <p>
   * The returned observable does not operate by default on a particular scheduler.
   *
   * @param directory
   *     the directory to list files from, not null
   *
   * @return a new observable that emits the files then completes
   */
  @Nonnull
  public Observable<File> getFilesInDirectory(@Nonnull final File directory) {
    checkNotNull(directory, "\'directory\' must not be null.");
    
    final Observable<File> listFiles = Observable
        .fromCallable(() -> Optional.ofNullable(directory.listFiles()))
        .flatMap(optionalFiles -> optionalFiles.isPresent() ?
            Observable.fromIterable(Arrays.asList(optionalFiles.get())) :
            Observable.error(makeError("Failed to get files in", directory, "Reason unknown")));
    
    return exists(directory).flatMapObservable(exists -> exists ?
        isFile(directory)
            .flatMapObservable(isFile -> isFile ?
                Observable.error(makeError(
                    "Failed to get files in",
                    directory,
                    "Expected a directory but found a file")) :
                listFiles) :
        Observable.error(makeError("Failed to get files in", directory, "The directory does not exist")));
  }
  
  /**
   * Rads the contents of the supplied file and converts it to a string using the supplied charset. The operation will
   * fail if the file does not exist, cannot be read from, or is actually a directory.
   * <p>
   * The returned single does not operate by default on a particular scheduler.
   *
   * @param file
   *     the file to read from, not null
   * @param charset
   *     the charset to use when decoding the data from the file, not null
   *
   * @return a new single that emits the file contents then completes
   */
  @Nonnull
  public Single<String> readStringFromFile(@Nonnull final File file, @Nonnull final Charset charset) {
    checkNotNull(file, "\'file\' must not be null.");
    checkNotNull(charset, "\'charset\' must not be null.");
    
    return readBytesFromFile(file).map(bytes -> new String(bytes, charset));
  }
  
  /**
   * Writes the supplied data to the supplied file, using the supplied charset for encoding. The operation will fail if
   * the file does not exist, cannot be written to, or is actually a directory.
   * <p>
   * <b>Caution: This operation will overwrite any existing content in the file.</b>
   * <p>
   * The returned completable does not operate by default on a particular scheduler.
   *
   * @param data
   *     the data to write to the file, not null
   * @param file
   *     the file to write the data to, not null
   * @param charset
   *     the charset to use when encoding the data
   *
   * @return a new completable that writes the data then completes
   */
  @Nonnull
  public Completable writeStringToFile(
      @Nonnull final String data,
      @Nonnull final File file,
      @Nonnull final Charset charset) {
    
    checkNotNull(file, "\'file\' must not be null.");
    checkNotNull(data, "\'data\' must not be null.");
    checkNotNull(charset, "\'charset\' must not be null.");
    
    return exists(file)
        .flatMapCompletable(exists -> exists ?
            Completable.complete() :
            Completable.error(new IOException("File does not exist.")))
        .andThen(writeBytesToFile(data.getBytes(charset), file));
  }
  
  /**
   * Reads the contents of the supplied file. The operation will fail if the file does not exist, cannot be read from,
   * or is actually a directory.
   * <p>
   * The returned single does not operate by default on a particular scheduler.
   *
   * @param file
   *     the file to read from, not null
   *
   * @return a new single that emits the file contents then completes
   */
  @Nonnull
  public Single<byte[]> readBytesFromFile(@Nonnull final File file) {
    checkNotNull(file, "\'file\' must not be null.");
    
    return Single.fromCallable(() -> FileUtils.readFileToByteArray(file));
  }
  
  /**
   * Writes the supplied data to the supplied file. The operation will fail if the file does not exist, cannot be
   * written to, or is actually a directory. This operation will override any existing content in the file.
   * <p>
   * The returned completable does not operate by default on a particular scheduler.
   *
   * @param data
   *     the string to write to the file, not null
   * @param file
   *     the file to write the string to, not null
   *
   * @return a new completable that writes the data then completes
   */
  @Nonnull
  public Completable writeBytesToFile(@Nonnull final byte[] data, @Nonnull final File file) {
    checkNotNull(file, "\'file\' must not be null.");
    checkNotNull(data, "\'data\' must not be null.");
    
    return exists(file)
        .flatMapCompletable(exists -> exists ?
            Completable.complete() :
            Completable.error(new IOException("File does not exist.")))
        .andThen(Completable.fromAction(() -> FileUtils.writeByteArrayToFile(file, data)));
  }
  
  /**
   * Determines if the supplied file/directory exists.
   * <p>
   * The returned single does not operate by default on a particular scheduler.
   *
   * @param file
   *     the file/directory to check, not null
   *
   * @return a new single that emits the result then completes
   */
  @Nonnull
  public Single<Boolean> exists(@Nonnull final File file) {
    checkNotNull(file, "\'file\' must not be null.");
    
    return Single.fromCallable(file::exists);
  }
  
  @Nonnull
  public Single<Long> sizeInBytes(@Nonnull final File file) {
    checkNotNull(file, "\'file\' must not be null.");
    
    return exists(file).flatMap(exists -> exists ?
        Single.fromCallable(file::length) :
        Single.error(new IOException(format("File \'%1$s\' does not exist.", file))));
  }
  
  /**
   * Determines if the supplied file is actually a file (as opposed to a directory). The operation will fail if the
   * file doesn't exist.
   *
   * @param file
   *     the file to check, not null
   *
   * @return a new single that emits the result then completes
   */
  @Nonnull
  public Single<Boolean> isFile(@Nonnull final File file) {
    checkNotNull(file, "\'file\' cannot be null.");
    
    return exists(file).flatMap(exists -> exists ?
        Single.just(file.isFile()) :
        Single.error(new IOException(format("File \'%1$s\' does not exist.", file))));
  }
  
  /**
   * Checks if the supplied directory is actually a directory (as opposed to a file). The operation will fail if the
   * directory doesn't exist.
   *
   * @param directory
   *     the directory to check, not null
   *
   * @return a new single that emits the result then completes
   */
  @Nonnull
  public Single<Boolean> isDirectory(@Nonnull final File directory) {
    checkNotNull(directory, "\'directory\' cannot be null.");
    
    return exists(directory).flatMap(exists -> exists ?
        Single.just(directory.isDirectory()) :
        Single.error(new IOException(format("Directory \'%1$s\' does not exist.", directory))));
  }
  
  /**
   * Creates an IOException with a message in the form "${description} '${file.getAbsolutePath()}'. ${reason}.".
   * <p>
   * For example, makeError("Failed to access", someFile, "File is in use") produces an IOException with the message
   * "Failed to access '~/somedir/somefile.txt'. File is in use."
   *
   * @return the exception
   */
  private IOException makeError(final String description, final File file, final String reason) {
    return new IOException(format("%1$S \'%2$s\'. %3$s.", description, file.getAbsolutePath(), reason));
  }
}