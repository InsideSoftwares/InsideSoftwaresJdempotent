package br.com.insidesoftwares.jdempotent.core.chain;

import br.com.insidesoftwares.jdempotent.core.model.ChainData;
import br.com.insidesoftwares.jdempotent.core.model.KeyValuePair;
import br.com.insidesoftwares.jdempotent.core.utils.IdempotentTestPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class JdempotentIgnoreAnnotationChainTest {

    @InjectMocks
    private JdempotentIgnoreAnnotationChain jdempotentIgnoreAnnotationChain;

    @Mock
    private JdempotentDefaultChain jdempotentDefaultChain;

    @Test
    void should_process() throws IllegalAccessException, NoSuchFieldException {
        //Given
        jdempotentIgnoreAnnotationChain.next(jdempotentDefaultChain);
        IdempotentTestPayload idempotentTestPayload = new IdempotentTestPayload();
        idempotentTestPayload.setAge(1l);
        ChainData chainData = new ChainData();
        chainData.setArgs(idempotentTestPayload);
        chainData.setDeclaredField(idempotentTestPayload.getClass().getDeclaredField("age"));

        //When
        KeyValuePair process = jdempotentIgnoreAnnotationChain.process(chainData);

        //Then
        assertNull(process.getKey());
        assertNull(process.getValue());
    }

    @Test
    void should_not_process_when_given_another_annotated_field() throws IllegalAccessException, NoSuchFieldException {
        //Given
        jdempotentIgnoreAnnotationChain.next(jdempotentDefaultChain);
        IdempotentTestPayload idempotentTestPayload = new IdempotentTestPayload();
        idempotentTestPayload.setName("name");
        ChainData chainData = new ChainData();
        chainData.setArgs(idempotentTestPayload);
        chainData.setDeclaredField(idempotentTestPayload.getClass().getDeclaredField("name"));

        //When
        jdempotentIgnoreAnnotationChain.process(chainData);

        //Then
        verify(jdempotentDefaultChain).process(eq(chainData));
    }
}
