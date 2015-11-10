import com.hp.sms.Application;
import com.hp.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
/**
 * Created by luj on 2015/11/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)

public class SmsServiceTest {
    @Autowired
    @Qualifier("cmppSender")
    SmsService cmppSms;

   @Autowired
   @Qualifier("simpleSMS")
   SmsService simpleSMS;
           ;
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test_cmpp(){
     System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
     cmppSms.sendSms("13000001111","中国");
    }
   @Test
   public void test_cat(){
   System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
    simpleSMS.sendSms("18071045790","haha");
   }

}
