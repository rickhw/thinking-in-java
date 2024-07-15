
### 3.2 [consumer] verify


```bash
count=$(seq 100 | xargs -I {} curl "http://127.0.0.1:9080/ip" -I -sL | grep "429" | wc -l); echo \"200\": $((100 - $count)), \"429\": $count

"200": 2, "429":       98
```