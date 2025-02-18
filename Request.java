
import java.io.Serializable;

public class Request implements Serializable{
    private static final long serialVersionUID = 1L;
	RequestType type;
	
	public Request(RequestType type) {
		this.type = type;
	}
}
