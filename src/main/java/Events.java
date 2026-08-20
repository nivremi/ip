public class Events extends Task {
    public String start;
    public String end;

    public Events(String description, String start, String end) {
        super(description);
        this.start = start;
        this.end = end;
        this.taskType = "E";
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