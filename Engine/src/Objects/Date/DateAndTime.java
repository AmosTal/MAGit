package Objects.Date;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateAndTime implements Serializable {

    private String dateAndTime;

    public DateAndTime(String date) {
        dateAndTime = date;
    }

    public DateAndTime(Long date) {
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss:SSS");
        Date d = new Date(date);
        LocalDateTime nisayon = Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        dateAndTime = myFormatObj.format(nisayon);
    }

    public DateAndTime() {
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss:SSS");
        LocalDateTime myDateobj = LocalDateTime.now();
        dateAndTime = myDateobj.format(myFormatObj);
    }

    public String getDate() {
        return dateAndTime;
    }

    public long getDateInSeconds()  {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        Date d = null;
        try {
            d = f.parse(dateAndTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert d != null;
        return  d.getTime();
    }
}

