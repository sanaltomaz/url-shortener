package com.tom.url_shortener.url.infrastructure.id;

import com.tom.url_shortener.url.domain.IdGenerator;
import io.hypersistence.tsid.TSID;
import org.springframework.stereotype.Component;

@Component
public class TsidGenerator implements IdGenerator {

    @Override
    public long nextId() {
        return TSID.fast().toLong();
    }
}
