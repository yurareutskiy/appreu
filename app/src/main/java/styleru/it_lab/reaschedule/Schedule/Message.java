package styleru.it_lab.reaschedule.Schedule;

public class Message {

    private int id;
    private String msg;
    private String date;

    public Message(int _id, String _msg, String _date)
    {
        id = _id;
        msg = _msg;
        date = _date;
    }

    public Message()
    {
        id = 0;
        msg = "";
        date = "";
    }

    public String getMsg(){return msg;}
    public String getDate() {return date;}
    public int getId() {return id;}
    public void setMsg(String _msg) { msg = _msg;}
    public void setDate(String _date) {date = _date;}

}
