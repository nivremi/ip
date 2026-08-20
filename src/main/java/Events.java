public class Events extends Task {
    public String start;
    public String end;

    public Events(String description, String start, String end) {
        super(description, TaskType.EVENT);
        this.start = start;
        this.end = end;
    }

    public String getStart() {
        return this.start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return this.end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public String toString(){
        return description + " (from: " + start + " to: " + end + ")";
    }
}
