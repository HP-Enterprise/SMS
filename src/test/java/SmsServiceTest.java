import com.hp.sms.Application;
import com.hp.sms.service.SmsService;
import com.hp.sms.utils.MsgUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;

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

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test_txt_cmpp(){
     System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
     cmppSms.sendSms("13000001111", "中国");
    }
    @Test
    public void test_bin_cmpp() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
        cmppSms.sendBinSms("13000001111", "123456".getBytes());
    }
    @Ignore("Not suitable for travis-ci")
    @Test
   public void test_txt_cat(){
   System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
    simpleSMS.sendSms("18071045790","haha");
   }

    @Test
    public void test_encode(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(MsgUtils.EncodeUCS2("中国ABCDEF123456"));
        String bytes="4E 2D 56 FD 00 41 00 42 00 43 00 44 00 45 00 46 00 31 00 32 00 33 00 34 00 35 00 36";
        System.out.println(MsgUtils.DecodeUCS2(bytes.replace(" ",""))+"|");

    }

}
