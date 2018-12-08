import genius.core.Domain;
import genius.core.DomainImpl;

public class Test {
    public static void main(String[] args) {

        Domain domain = null;
        try {
            domain = new DomainImpl("etc/templates/pie/pie_domain.xml");
        } catch (Exception e) {

        }


    }
}
