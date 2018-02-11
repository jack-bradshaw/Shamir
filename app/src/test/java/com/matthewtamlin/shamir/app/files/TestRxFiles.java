package com.matthewtamlin.shamir.app.files;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

//TODO replace test directory with TemporaryFolder test rule
@SuppressWarnings("ConstantConditions")
public class TestRxFiles {
  private RxFiles rxFiles;
  
  private File testDirectory;
  
  @Before
  public void setup() throws IOException {
    rxFiles = new RxFiles();
    testDirectory = new File("TemporaryDirectoryForTesting");
    
    // Each test needs a clean directory to avoid interference
    FileUtils.deleteDirectory(testDirectory);
    FileUtils.forceMkdir(testDirectory);
  }
  
  @After
  public void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCreateDirectory_nullDirectory() {
    rxFiles.createDirectory(null);
  }
  
  @Test
  public void testCreateDirectory_directoryAlreadyExistsAsAFile() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    rxFiles
        .createDirectory(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testCreateDirectory_directoryAlreadyExistsAsADirectory() throws IOException {
    final File directory = new File(testDirectory, "test");
    FileUtils.forceMkdir(directory);
    
    rxFiles
        .createDirectory(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
  }
  
  @Test
  public void testCreateDirectory_directoryDoesNotExist() {
    final File directory = new File(testDirectory, "test");
    
    rxFiles
        .createDirectory(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("Directory was not created.", directory.exists(), is(true));
  }
  
  @Test
  public void testCreateDirectory_parentDirectoryDoesNotExist() {
    final File parentDirectory = new File(testDirectory, "test");
    final File childDirectory = new File(parentDirectory, "test");
    
    rxFiles
        .createDirectory(childDirectory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("Parent directory was not created.", parentDirectory.exists(), is(true));
    assertThat("Child directory was not created.", childDirectory.exists(), is(true));
    assertThat("Child directory is not in parent directory.", childDirectory.getParentFile(), is(parentDirectory));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCreateNewFile_nullFile() {
    rxFiles.createNewFile(null);
  }
  
  @Test
  public void testCreateNewFile_fileAlreadyExistsAsAFile() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    rxFiles
        .createNewFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testCreateNewFile_fileAlreadyExistsAsADirectory() throws IOException {
    final File directory = new File(testDirectory, "test");
    FileUtils.forceMkdir(directory);
    
    rxFiles
        .createNewFile(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testCreateNewFile_parentDirectoryDoesNotExist() {
    final File directory = new File(testDirectory, "test");
    final File file = new File(directory, "test.txt");
    
    rxFiles
        .createNewFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
    
    assertThat("Directory was created.", directory.exists(), is(false));
    assertThat("File was created.", file.exists(), is(false));
  }
  
  @Test
  public void testCreateNewFile_parentDirectoryExists() {
    final File directory = new File(testDirectory, "test");
    final File file = new File(directory, "test.txt");
    
    directory.mkdirs();
    
    rxFiles
        .createNewFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("File was not created.", file.exists(), is(true));
    assertThat("File was created in the wrong directory.", file.getParentFile(), is(directory));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testGetFilesInDirectory_nullDirectory() {
    rxFiles.getFilesInDirectory(null);
  }
  
  @Test
  public void testGetFilesInDirectory_directoryIsActuallyAFile() {
    final File file = new File(testDirectory, "test");
    
    rxFiles
        .getFilesInDirectory(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testGetFilesInDirectory_directoryDoesNotExist() throws IOException {
    final File directory = new File(testDirectory, "test");
    
    rxFiles
        .getFilesInDirectory(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testGetFilesInDirectory_directoryContainsNoFiles() throws IOException {
    final File directory = new File(testDirectory, "test");
    FileUtils.forceMkdir(directory);
    
    rxFiles
        .getFilesInDirectory(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertNoValues();
  }
  
  @Test
  public void testGetFilesInDirectory_directoryContainsOneFile() throws IOException {
    final File directory = new File(testDirectory, "test");
    FileUtils.forceMkdir(directory);
    
    final File file = new File(directory, "test.txt");
    file.createNewFile();
    
    rxFiles
        .getFilesInDirectory(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValues(file);
  }
  
  @Test
  public void testGetFilesInDirectory_directoryContainsMultipleFiles() throws IOException {
    final File directory = new File(testDirectory, "test");
    FileUtils.forceMkdir(directory);
    
    final File file1 = new File(directory, "test1.txt");
    final File file2 = new File(directory, "test2.txt");
    file1.createNewFile();
    file2.createNewFile();
    
    rxFiles
        .getFilesInDirectory(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValues(file1, file2);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testReadStringFromFile_fileIsNull() {
    rxFiles.readStringFromFile(null, Charset.defaultCharset());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testReadStringFromFile_charsetIsNull() {
    rxFiles.readStringFromFile(new File(testDirectory, "test"), null);
  }
  
  @Test
  public void testReadStringFromFile_fileIsActuallyADirectory() throws IOException {
    final File directory = new File(testDirectory, "test");
    FileUtils.forceMkdir(directory);
    
    rxFiles
        .readStringFromFile(directory, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testReadStringFromFile_fileDoesNotExist() {
    final File file = new File(testDirectory, "test.txt");
    
    rxFiles
        .readStringFromFile(file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testReadStringFromFile_fileIsEmpty() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    rxFiles
        .readStringFromFile(file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue("");
  }
  
  @Test
  public void testReadStringFromFile_fileContainsOnlyNewLine() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String content = "\n";
    
    FileUtils.write(file, content, Charset.defaultCharset());
    
    rxFiles
        .readStringFromFile(file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(content);
  }
  
  @Test
  public void testReadStringFromFile_fileContainsOneLine() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String content = "test";
    
    FileUtils.write(file, content, Charset.defaultCharset());
    
    rxFiles
        .readStringFromFile(file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(content);
  }
  
  @Test
  public void testReadStringFromFile_fileContainsMultipleLines() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String content = "test1\ntest2";
    
    FileUtils.write(file, content, Charset.defaultCharset());
    
    rxFiles
        .readStringFromFile(file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(content);
  }
  
  @Test
  public void testReadLineAsString_fileEndsWithNewLine() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String content = "test1\ntest2\n";
    
    FileUtils.write(file, content, Charset.defaultCharset());
    
    rxFiles
        .readStringFromFile(file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(content);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testWriteStringToFile_nullFile() {
    rxFiles.writeStringToFile("test", null, Charset.defaultCharset());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testWriteStringToFile_nullData() {
    rxFiles.writeStringToFile(null, new File(testDirectory, "test"), Charset.defaultCharset());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testWriteStringToFile_nullCharset() {
    rxFiles.writeStringToFile("test", new File(testDirectory, "test"), null);
  }
  
  @Test
  public void testWriteStringToFile_fileIsActuallyADirectory() throws IOException {
    final File directory = new File(testDirectory, "test");
    FileUtils.forceMkdir(directory);
    
    rxFiles
        .writeStringToFile("test", directory, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testWriteStringToFile_fileDoesNotExist() {
    final File file = new File(testDirectory, "test.txt");
    
    rxFiles
        .writeStringToFile("test", file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testWriteStringToFile_dataIsEmpty() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String contents = "";
    
    rxFiles
        .writeStringToFile(contents, file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("File has wrong contents.", FileUtils.readFileToString(file, Charset.defaultCharset()), is(contents));
  }
  
  @Test
  public void testWriteStringToFile_dataContainsOnlyNewLine() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String contents = "\n";
    
    rxFiles
        .writeStringToFile(contents, file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("File has wrong contents.", FileUtils.readFileToString(file, Charset.defaultCharset()), is(contents));
  }
  
  @Test
  public void testWriteStringToFile_dataContainsOneLine() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String contents = "test";
    
    rxFiles
        .writeStringToFile(contents, file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("String does not match file content.",
        FileUtils.readFileToString(file, Charset.defaultCharset()),
        is(contents));
  }
  
  @Test
  public void testWriteStringToFile_dataContainsMultipleLines() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String contents = "test1\ntest2";
    
    rxFiles
        .writeStringToFile(contents, file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("String does not match file content.",
        FileUtils.readFileToString(file, Charset.defaultCharset()),
        is(contents));
  }
  
  @Test
  public void testWriteStringToFile_dataEndsWithNewLine() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String contents = "test1\ntest2\n";
    
    rxFiles
        .writeStringToFile(contents, file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat("String does not match file content.",
        FileUtils.readFileToString(file, Charset.defaultCharset()),
        is(contents));
  }
  
  @Test
  public void testWriteStringToFile_fileIsNotEmptyBeforeWriting() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final String contents = "new content";
    
    FileUtils.write(file, "old content", Charset.defaultCharset());
    
    rxFiles
        .writeStringToFile(contents, file, Charset.defaultCharset())
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    assertThat(
        "String does not match file content.",
        FileUtils.readFileToString(file, Charset.defaultCharset()),
        is(contents));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testReadBytesFromFile_nullFile() {
    rxFiles.readBytesFromFile(null);
  }
  
  @Test
  public void testReadBytesFromFile_fileDoesNotExist() {
    final File file = new File(testDirectory, "test.txt");
    
    rxFiles
        .readBytesFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testReadBytesFromFile_fileIsActuallyADirectory() throws IOException {
    final File file = new File(testDirectory, "test");
    FileUtils.forceMkdir(file);
    
    rxFiles
        .readBytesFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testReadBytesFromFile_fileIsEmpty() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final byte[] data = new byte[0];
    
    FileUtils.writeByteArrayToFile(file, data);
    
    rxFiles
        .readBytesFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(value -> Arrays.equals(data, value));
  }
  
  @Test
  public void testReadBytesFromFile_fileContainsOneByte() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final byte[] data = new byte[]{127};
    
    FileUtils.writeByteArrayToFile(file, data);
    
    rxFiles
        .readBytesFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(value -> Arrays.equals(data, value));
  }
  
  @Test
  public void testReadBytesFromFile_fileContainsMultipleBytes() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final byte[] data = new byte[]{0, 127};
    
    FileUtils.writeByteArrayToFile(file, data);
    
    rxFiles
        .readBytesFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(value -> Arrays.equals(data, value));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testWriteBytesToFile_nullFile() {
    rxFiles.writeBytesToFile(new byte[0], null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testWriteBytesToFile_nullData() {
    rxFiles.writeBytesToFile(null, new File(testDirectory, ""));
  }
  
  @Test
  public void testWriteBytesToFile_fileDoesNotExist() {
    final File file = new File(testDirectory, "test.txt");
    
    rxFiles
        .writeBytesToFile(new byte[0], file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testWriteBytesToFile_fileIsActuallyADirectory() throws IOException {
    final File file = new File(testDirectory, "test");
    FileUtils.forceMkdir(file);
    
    rxFiles
        .writeBytesToFile(new byte[0], file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testWriteBytesToFile_dataIsEmpty() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final byte[] data = new byte[0];
    
    rxFiles
        .writeBytesToFile(data, file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
  }
  
  @Test
  public void testWriteBytesToFile_dataContainsOneByte() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final byte[] bytes = new byte[]{127};
    
    FileUtils.writeByteArrayToFile(file, bytes);
    
    rxFiles
        .readBytesFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
  }
  
  @Test
  public void testWriteBytesToFile_dataContainsMultipleBytes() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    final byte[] bytes = new byte[]{0, 127};
    
    FileUtils.writeByteArrayToFile(file, bytes);
    
    rxFiles
        .readBytesFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testExists_nullFile() {
    rxFiles.exists(null);
  }
  
  @Test
  public void testExists_fileThatDoesNotExist() {
    final File file = new File(testDirectory, "test.txt");
    
    rxFiles
        .exists(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testExists_fileThatExists() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    rxFiles
        .exists(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
  
  @Test
  public void testExists_directoryThatDoesNotExist() {
    final File file = new File(testDirectory, "test");
    
    rxFiles
        .exists(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testExists_directoryThatExists() {
    final File file = new File(testDirectory, "test");
    file.mkdirs();
    
    rxFiles
        .exists(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSizeInBytes_nullFile() {
    rxFiles.sizeInBytes(null);
  }
  
  @Test
  public void testSizeInBytes_nullFileThatDoesNotExist() {
    final File file = new File(testDirectory, "test");
    
    rxFiles
        .sizeInBytes(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testSizeInBytes_emptyFile() throws IOException {
    final File file = new File(testDirectory, "test");
    file.createNewFile();
    
    rxFiles
        .sizeInBytes(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(0L);
  }
  
  @Test
  public void testSizeInBytes_nonEmptyFile() throws IOException {
    final File file = new File(testDirectory, "test");
    file.createNewFile();
    
    final byte[] data = new byte[]{10};
    FileUtils.writeByteArrayToFile(file, data);
    
    rxFiles
        .sizeInBytes(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(1L);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIsFile_nullFile() {
    rxFiles.isFile(null);
  }
  
  @Test
  public void testIsFile_fileDoesNotExist() {
    final File file = new File(testDirectory, "test.txt");
    
    rxFiles
        .isFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testIsFile_fileIsActuallyADirectory() throws IOException {
    final File file = new File(testDirectory, "test");
    FileUtils.forceMkdir(file);
    
    rxFiles
        .isFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testIsFile_fileIsActuallyAFile() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    rxFiles
        .isFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIsDirectory_nullFile() {
    rxFiles.isDirectory(null);
  }
  
  @Test
  public void testIsDirectory_directoryDoesNotExist() {
    final File directory = new File(testDirectory, "test");
    
    rxFiles
        .isDirectory(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testIsDirectory_directoryIsActuallyAFile() throws IOException {
    final File file = new File(testDirectory, "test.txt");
    file.createNewFile();
    
    rxFiles
        .isDirectory(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testIsDirectory_directoryIsActuallyADirectory() throws IOException {
    final File file = new File(testDirectory, "test");
    FileUtils.forceMkdir(file);
    
    rxFiles
        .isDirectory(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
}