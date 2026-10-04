public class Task {
    private final int id;
    private final String data;

    public Task(int id, String data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", data='" + data + "'}";
    }
}
