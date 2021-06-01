/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.taskie.networking

import com.raywenderlich.android.taskie.model.Result
import com.raywenderlich.android.taskie.model.Task
import com.raywenderlich.android.taskie.model.UserProfile
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.LoginResponse
import com.raywenderlich.android.taskie.model.response.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

class RemoteApi(private val apiService: RemoteApiService) {

    fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (Result<String>) -> Unit) {

        apiService.loginUser(userDataRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()

                if (loginResponse == null || loginResponse.token.isNullOrEmpty()) {
                    onUserLoggedIn(Result.Failure(NullPointerException("No response body")))
                } else {
                    onUserLoggedIn(Result.Success(loginResponse.token))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, error: Throwable) {
                onUserLoggedIn(Result.Failure(error))
            }
        })
    }

    fun registerUser(
        userDataRequest: UserDataRequest,
        onUserCreated: (Result<String>) -> Unit
    ) {
        apiService.registerUser(userDataRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                val message = response.body()?.message

                if (message == null) {
                    onUserCreated(Result.Failure(NullPointerException("No response body!")))
                    return
                }

                onUserCreated(Result.Success(message))
            }

            override fun onFailure(call: Call<RegisterResponse>, error: Throwable) {
                onUserCreated(Result.Failure(error))
            }
        })
    }

    suspend fun getTasks(): Result<List<Task>> {
        return try {
            val data = apiService.getNotes()

            Result.Success(data.notes.filter { !it.isCompleted })
        } catch (error: Throwable) {
            Result.Failure(error)
        }
    }

    suspend fun deleteTask(taskId: String): Result<String> {
        return try {
            val data = apiService.deleteNote(taskId)

            Result.Success(data.message)
        } catch (error: Throwable) {
            Result.Failure(error)
        }
    }

    suspend fun completeTask(taskId: String): Result<String> {
        return try {
            val data = apiService.completeTask(taskId)

            Result.Success(data.message)
        } catch (error: Throwable) {
            Result.Failure(error)
        }
    }

    suspend fun addTask(addTaskRequest: AddTaskRequest): Result<Task> {
        return try {
            val data = apiService.addTask(addTaskRequest)

            Result.Success(data)
        } catch (error: Throwable) {
            Result.Failure(error)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val notesResult = getTasks()

            if (notesResult is Result.Failure) {
                Result.Failure(notesResult.error)
            } else {
                val notes = notesResult as Result.Success
                val data = apiService.getMyProfile()

                if (data.email == null || data.name == null) {
                    Result.Failure(NullPointerException("No data available!"))
                } else {
                    Result.Success(
                        UserProfile(
                            email = data.email,
                            name = data.name,
                            numberOfNotes = notes.data.size
                        )
                    )
                }
            }
        } catch (error: Throwable) {
            Result.Failure(error)
        }
    }
}