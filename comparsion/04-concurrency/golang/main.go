package main

import (
	"fmt"
	"net/http"
	"sync"
)

func main() {
	var wg sync.WaitGroup
	for i := 0; i < 1000; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			resp, err := http.Get("http://example.com")
			if err == nil {
				fmt.Println(resp.Status)
			}
		}()
	}
	wg.Wait()
}
