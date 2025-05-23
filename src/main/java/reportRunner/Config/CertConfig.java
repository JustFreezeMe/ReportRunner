package reportRunner.Config;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

@Service
public class CertConfig {

    private final ConfluenceConfig confluenceConfig;

    public CertConfig(ConfluenceConfig confluenceConfig) {
        this.confluenceConfig = confluenceConfig;
    }

    @SneakyThrows
    public SSLContext configureSsl() {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new FileInputStream(confluenceConfig.getCertName());
        Certificate cert = cf.generateCertificate(is);
        is.close();

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("mycert", cert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }
}
