package main

import (
	"fmt"
	"os"
	"sync"
	"time"
)

// Task represents a unit of work.
type Task struct {
	ID   int
	Data string
}

// processTask simulates computational work.
func processTask(task Task, workerID int) string {
	fmt.Printf(
		"[WORKER-%d] Processing Task-%d\n",
		workerID,
		task.ID,
	)

	// Simulate computational work.
	time.Sleep(time.Duration(300+workerID*50) * time.Millisecond)

	result := fmt.Sprintf(
		"Worker-%d processed Task-%d | Input: %s | Result: %s",
		workerID,
		task.ID,
		task.Data,
		toUpper(task.Data),
	)

	return result
}

// toUpper converts lowercase ASCII characters to uppercase.
func toUpper(text string) string {
	result := ""

	for _, character := range text {
		if character >= 'a' && character <= 'z' {
			character = character - ('a' - 'A')
		}

		result += string(character)
	}

	return result
}

// worker reads tasks from the channel and processes them.
func worker(
	workerID int,
	tasks <-chan Task,
	results chan<- string,
	wg *sync.WaitGroup,
) {
	defer wg.Done()

	fmt.Printf(
		"[WORKER-%d] Started.\n",
		workerID,
	)

	for task := range tasks {
		result := processTask(task, workerID)

		results <- result

		fmt.Printf(
			"[WORKER-%d] Completed Task-%d\n",
			workerID,
			task.ID,
		)
	}

	fmt.Printf(
		"[WORKER-%d] Finished.\n",
		workerID,
	)
}

// writeResults receives processed results and writes them to a file.
func writeResults(
	results <-chan string,
	filePath string,
	wg *sync.WaitGroup,
) {
	defer wg.Done()

	// Create or overwrite the results file for a clean run.
	file, err := os.OpenFile(
		filePath,
		os.O_CREATE|os.O_WRONLY|os.O_TRUNC,
		0644,
	)

	if err != nil {
		fmt.Printf(
			"[ERROR] Could not open results file: %v\n",
			err,
		)

		return
	}

	// Ensure the file is closed when this function exits.
	defer func() {
		if err := file.Close(); err != nil {
			fmt.Printf(
				"[ERROR] Could not close results file: %v\n",
				err,
			)
		}
	}()

	for result := range results {
		_, err := file.WriteString(result + "\n")

		if err != nil {
			fmt.Printf(
				"[ERROR] Could not write result: %v\n",
				err,
			)

			continue
		}
	}

	fmt.Println("[RESULTS] All results written successfully.")
}

func main() {
	fmt.Println("==============================================")
	fmt.Println("      GO MULTI-THREADED DATA PROCESSING")
	fmt.Println("==============================================")

	outputFile := "data/results.txt"

	// Create the data directory if it does not exist.
	err := os.MkdirAll("data", 0755)

	if err != nil {
		fmt.Printf(
			"[ERROR] Could not create data directory: %v\n",
			err,
		)

		return
	}

	// Shared task channel.
	tasks := make(chan Task)

	// Shared results channel.
	results := make(chan string)

	// WaitGroup for worker goroutines.
	var workerWaitGroup sync.WaitGroup

	// WaitGroup for the result writer.
	var resultWaitGroup sync.WaitGroup

	startTime := time.Now()

	fmt.Println("[SYSTEM] Creating 20 tasks...")
	fmt.Println("[SYSTEM] Starting 4 worker goroutines...")
	fmt.Println()

	// Start four worker goroutines.
	for i := 1; i <= 4; i++ {
		workerWaitGroup.Add(1)

		go worker(
			i,
			tasks,
			results,
			&workerWaitGroup,
		)
	}

	// Start the result writer goroutine.
	resultWaitGroup.Add(1)

	go writeResults(
		results,
		outputFile,
		&resultWaitGroup,
	)

	// Send tasks to workers.
	go func() {
		for i := 1; i <= 20; i++ {
			task := Task{
				ID:   i,
				Data: fmt.Sprintf("data_record_%d", i),
			}

			fmt.Printf(
				"[QUEUE] Added Task-%d\n",
				task.ID,
			)

			tasks <- task
		}

		// No more tasks will be submitted.
		close(tasks)

		fmt.Println(
			"[QUEUE] All tasks submitted.",
		)
	}()

	// Wait until all workers have completed.
	workerWaitGroup.Wait()

	// No more results will be produced.
	close(results)

	// Wait until the result writer has finished.
	resultWaitGroup.Wait()

	elapsed := time.Since(startTime)

	fmt.Println()
	fmt.Println("==============================================")
	fmt.Println("           PROCESSING COMPLETED")
	fmt.Println("==============================================")

	fmt.Printf(
		"[SYSTEM] Results saved to: %s\n",
		outputFile,
	)

	fmt.Printf(
		"[SYSTEM] Execution time: %v\n",
		elapsed,
	)

	fmt.Println(
		"[SYSTEM] All worker goroutines terminated safely.",
	)
}
