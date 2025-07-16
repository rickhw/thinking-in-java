@RestController
@RequestMapping("/raft")
public class RaftController {

    @Autowired
    private RaftNode node;

    @PostMapping("/vote")
    public ResponseEntity<Boolean> vote(@RequestBody int term) {
        return ResponseEntity.ok(node.requestVote(term));
    }

    @PostMapping("/append")
    public ResponseEntity<Void> append(@RequestBody AppendEntriesRequest req) {
        node.receiveAppendEntries(req);
        return ResponseEntity.ok().build();
    }
}
