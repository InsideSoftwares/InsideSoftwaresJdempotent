package br.com.insidesoftwares.jdempotent.core.chain;

import br.com.insidesoftwares.jdempotent.core.model.ChainData;
import br.com.insidesoftwares.jdempotent.core.model.KeyValuePair;
import br.com.insidesoftwares.jdempotent.core.utils.IdempotentTestPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class JdempotentNoAnnotationChainTest {

    @InjectMocks
    private JdempotentNoAnnotationChain jdempotentNoAnnotationChain;

    @Mock
    private JdempotentDefaultChain jdempotentDefaultChain;

    @Test
    void should_process_with_no_annotation() throws IllegalAccessException, NoSuchFieldException {
        //Given
        jdempotentNoAnnotationChain.next(jdempotentDefaultChain);
        MockData mockData = new MockData();
        ChainData chainData = new ChainData();
        chainData.setArgs(mockData);
        chainData.setDeclaredField(mockData.getClass().getDeclaredField("name"));

        //When
        KeyValuePair process = jdempotentNoAnnotationChain.process(chainData);

        //Then
        assertEquals("name", process.getKey());
        assertNull(process.getValue());
    }


    @Test
    void should_process_with_another_annotated_property() throws IllegalAccessException, NoSuchFieldException {
        //Given
        jdempotentNoAnnotationChain.next(jdempotentDefaultChain);
        IdempotentTestPayload idempotentTestPayload = new IdempotentTestPayload();
        idempotentTestPayload.setEventId(1l);
        ChainData chainData = new ChainData();
        chainData.setArgs(idempotentTestPayload);
        chainData.setDeclaredField(idempotentTestPayload.getClass().getDeclaredField("eventId"));

        //When
        KeyValuePair process = jdempotentNoAnnotationChain.process(chainData);

        //Then
        verify(jdempotentDefaultChain).process(eq(chainData));
    }

    static class MockData{
        private String name;
    }
}
