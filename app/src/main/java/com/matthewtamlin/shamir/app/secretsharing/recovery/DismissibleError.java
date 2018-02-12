/*
 * Copyright 2018 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.shamir.app.secretsharing.recovery;

public enum DismissibleError {
  SHARE_FILE_DOES_NOT_EXIST,
  SHARE_FILE_IS_MALFORMED,
  RECOVERY_SCHEME_FILE_DOES_NOT_EXIST,
  RECOVERY_SCHEME_IS_MALFORMED,
  OUTPUT_DIRECTORY_CANNOT_BE_CREATED,
  OUTPUT_DIRECTORY_IS_NOT_CLEAN,
  CANNOT_CREATE_RECOVERED_SECRET_FILE,
  CANNOT_WRITE_TO_RECOVERED_SECRET_FILE,
  RECOVERY_FAILED,
  FILESYSTEM_ERROR
}