package swag49.web.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement(name = "credentials")
@XmlAccessorType(XmlAccessType.FIELD)
public class TokenDTO {
    @XmlElement(name="token")
    private UUID token;
    @XmlElement(name="user")
    private Long userId;

    public TokenDTO() {

    }

    public TokenDTO(UUID token, Long userId) {
        this.token = token;
        this.userId = userId;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}