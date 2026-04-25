package com.wallet.walletapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.attachments")
public class AttachmentProperties {

    private DataSize maxFileSize = DataSize.ofMegabytes(5);
}
