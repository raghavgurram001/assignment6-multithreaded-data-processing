# Assignment 6: Multi-threaded Data Processing System

## Overview

This project implements a multi-threaded data processing system using **Java** and **Go**. The system creates a collection of data-processing tasks, places the tasks into a shared processing mechanism, and uses multiple concurrent workers to process the tasks.

The purpose of the assignment is to demonstrate fundamental concurrency and synchronization concepts in both programming languages. The Java implementation uses threads managed through `ExecutorService`, synchronized methods, and `ReentrantLock`. The Go implementation uses goroutines, channels, and `sync.WaitGroup`.

Both implementations simulate processing delays, provide execution logging, handle errors, safely coordinate worker termination, and save the processed results to a text file.

## Objectives

The project demonstrates the following concurrency concepts:

- Creation and processing of multiple tasks.
- Concurrent execution using multiple workers.
- Safe access to shared resources.
- Synchronization and coordination between workers.
- Simulation of processing delays.
- Error and exception handling.
- Safe worker termination.
- Logging of task-processing events.
- Writing processed results to an output file.
- Comparison of Java threads with Go goroutines and channels.

## Project Structure

```text
assignment6-multithreaded-data-processing/
│
├── README.md
├── .gitignore
│
├── java/
│   ├── DataProcessingSystem.java
│   ├── Task.java
│   └── data/
│       └── results.txt
│
├── go/
│   ├── main.go
│   └── data/
│       └── results.txt
│
└── screenshots/
    ├── java-code.png
    ├── java-output.png
    ├── go-code.png
    └── go-output.png
```

The `results.txt` files are generated automatically when the programs run and are excluded from Git tracking through `.gitignore`.

## Java Implementation

The Java implementation uses a shared `TaskQueue` containing the processing tasks. The `addTask()` and `getTask()` methods are synchronized so that multiple worker threads cannot modify or retrieve tasks from the queue in an unsafe manner.

Four worker threads are created using Java's `ExecutorService`. Each worker retrieves a task from the queue, simulates processing using `Thread.sleep()`, converts the task data to uppercase, and writes the processed result to the output file.

A `ReentrantLock` is used by the `ResultWriter` class to ensure that multiple worker threads do not write to the results file simultaneously. This prevents concurrent file-access problems and protects the shared output resource.

### Java Compilation

Navigate to the Java directory:

```bash
cd ~/assignment6-multithreaded-data-processing/java
```

Compile both Java source files:

```bash
javac Task.java DataProcessingSystem.java
```

### Java Execution

Run the application:

```bash
java DataProcessingSystem
```

The program creates 20 tasks and processes them using four worker threads.

### Java Results

The processed results can be viewed using:

```bash
cat data/results.txt
```

To verify that all 20 tasks were processed:

```bash
wc -l data/results.txt
```

The expected result is:

```text
20 data/results.txt
```

The successful Java execution completed in approximately **2302 milliseconds**, with the queue empty and all worker threads terminated safely.

## Go Implementation

The Go implementation provides the same data-processing functionality using Go's concurrency model.

Four worker goroutines process tasks concurrently. A `tasks` channel is used to distribute tasks to the workers, while a `results` channel transfers completed results to a dedicated result-writing goroutine.

The program uses `sync.WaitGroup` to coordinate the termination of the worker goroutines and the result-writing goroutine. The task channel is closed after all 20 tasks have been submitted, allowing the workers to finish safely. After all workers have completed, the results channel is closed.

The result writer uses `defer` to ensure that the output file is closed properly after processing is complete.

### Go Execution

Navigate to the Go directory:

```bash
cd ~/assignment6-multithreaded-data-processing/go
```

Format the Go source code:

```bash
gofmt -w main.go
```

Run the program:

```bash
go run main.go
```

### Building the Go Application

The application can also be compiled into an executable:

```bash
go build -o data_processing_system
```

Run the compiled application:

```bash
./data_processing_system
```

### Go Results

View the processed results:

```bash
cat data/results.txt
```

Verify that all 20 tasks were processed:

```bash
wc -l data/results.txt
```

The expected result is:

```text
20 data/results.txt
```

The successful Go execution completed in approximately **2.25 seconds**, and all worker goroutines terminated safely.

## Concurrency and Synchronization

The Java and Go implementations solve the same problem using different concurrency mechanisms.

Java uses `ExecutorService` to manage a fixed pool of four worker threads. The shared task queue uses synchronized methods, while `ReentrantLock` protects concurrent writes to the results file. `awaitTermination()` ensures that the main program waits for the worker threads to finish.

Go uses goroutines instead of explicitly managed operating-system-style threads. Channels provide safe communication between the task producer, workers, and result writer. `sync.WaitGroup` coordinates the completion of concurrent goroutines.

The Go implementation therefore relies more heavily on communication through channels, while the Java implementation combines thread pools with explicit synchronization and locking.

## Error Handling

The Java implementation uses `try-catch` blocks to handle `InterruptedException` and `IOException`. If a worker is interrupted, the thread's interrupted status is restored using:

```java
Thread.currentThread().interrupt();
```

The Java program also handles the possibility that the worker pool does not terminate within the expected time.

The Go implementation explicitly checks errors returned by file and directory operations. The output file is closed using `defer`, ensuring that the resource is released when the result-writing function completes.

These mechanisms help prevent unexpected termination and ensure that resources are properly managed.

## Logging

Both programs provide console logging to make the concurrent execution observable.

The applications report when:

- Workers start.
- Tasks are added.
- Tasks are processed.
- Tasks are completed.
- Results are written.
- Errors occur.
- Workers terminate.
- The complete processing system finishes.

Because multiple workers execute concurrently, the order of task-processing messages may differ between executions. This is expected behavior in a concurrent system.

## Testing

The system was tested using **20 data-processing tasks** and **4 concurrent workers** in each implementation.

The Java implementation successfully processed all 20 tasks and reported:

```text
[SYSTEM] Remaining queue empty: true
[SYSTEM] Results saved to: data/results.txt
[SYSTEM] Execution time: 2302 ms
[SYSTEM] All workers terminated safely.
```

The Go implementation successfully processed all 20 tasks and reported:

```text
[RESULTS] All results written successfully.

==============================================
           PROCESSING COMPLETED
==============================================
[SYSTEM] Results saved to: data/results.txt
[SYSTEM] Execution time: 2.253662151s
[SYSTEM] All worker goroutines terminated safely.
```

The result files should contain exactly 20 processed records after each successful execution.

## Screenshots

The `screenshots` directory contains evidence of the implementation and execution.

### Java Source Code

The `java-code.png` screenshot should show the Java implementation, particularly the synchronized task queue, `ReentrantLock`, worker implementation, and `ExecutorService`.

### Java Execution

The `java-output.png` screenshot should show the successful terminal execution demonstrating task processing, worker completion, an empty queue, and safe termination.

### Go Source Code

The `go-code.png` screenshot should show the Go implementation, including goroutines, channels, `sync.WaitGroup`, error handling, and deferred file cleanup.

### Go Execution

The `go-output.png` screenshot should show the successful Go terminal execution demonstrating concurrent processing and safe goroutine termination.

## GitHub Repository

The project is intended to be submitted through a GitHub repository named:

```text
assignment6-multithreaded-data-processing
```

After creating the repository, the project can be uploaded using:

```bash
cd ~/assignment6-multithreaded-data-processing

git init

git add .

git commit -m "Implement multi-threaded data processing system in Java and Go"

git branch -M main

git remote add origin https://github.com/raghavgurram001/assignment6-multithreaded-data-processing

git push -u origin main
```

Replace `YOUR_USERNAME` with the appropriate GitHub username.

## Requirements

### Java

The Java implementation requires:

```text
Java Development Kit (JDK)
```

Verify the installation with:

```bash
java -version
javac -version
```

### Go

The Go implementation requires:

```text
Go programming language
```

Verify the installation with:

```bash
go version
```

## Expected Output

After successful execution, both implementations should produce:

```text
20 processed tasks
4 concurrent workers
1 results.txt output file
Safe worker termination
```

Each result follows the general format:

```text
Worker-1 processed Task-1 | Input: data_record_1 | Result: DATA_RECORD_1
```

The exact worker assigned to each task may vary because concurrent workers compete for available tasks.

## Conclusion

This project demonstrates how the same multi-threaded data-processing problem can be implemented using two different concurrency models. Java provides explicit thread-pool management and synchronization mechanisms, while Go provides lightweight goroutines and channel-based communication. Both implementations successfully process 20 tasks concurrently, protect shared resources, handle errors, produce execution logs, and terminate workers safely.
