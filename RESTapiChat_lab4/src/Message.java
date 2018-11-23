import org.json.JSONObject;

public class Message {
    private int id;
    private String msg;

    private int authorId;
    private String author;

    public Message(int id, String msg, int authorId, String author) {
        this.id = id;
        this.msg = msg;
        this.authorId = authorId;
        this.author = author;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject
                .put("message", this.msg)
                .put("id", this.id)
                .put("author", this.author)
                .put("authorId", this.authorId);

        return jsonObject;
    }
}
