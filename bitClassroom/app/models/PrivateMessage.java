package models;

import com.avaje.ebean.Model;
import models.user.User;
import org.joda.time.DateTime;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * Created by NN on 20.9.2015.
 */

@Entity
public class PrivateMessage extends Model {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    @Constraints.Required
    private String content;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    @Column
    private Integer status;

    @Column(name = "create_date", updatable = false, columnDefinition = "datetime")
    private DateTime creationDate = new DateTime();


    public static Finder<Long, PrivateMessage> finder = new Finder<Long, PrivateMessage>(PrivateMessage.class);


    public PrivateMessage(String content, User sender, User receiver){

        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
    }

    public PrivateMessage(){

        this.content = "No conent";
        this.sender = null;
        this.receiver = null;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiveer() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public static PrivateMessage create(String content, User sender, User receiver) {
        PrivateMessage newMessage = new PrivateMessage(content, sender, receiver);
        newMessage.save();
        return newMessage;
    }

    public static void deleteMsg(Long id){

        finder.ref(id).delete();
    }

}