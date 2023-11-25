package br.com.insidesoftwares.jdempotent.core.callback;

import br.com.insidesoftwares.jdempotent.core.constant.CryptographyAlgorithm;
import br.com.insidesoftwares.jdempotent.core.datasource.InMemoryIdempotentRepository;
import br.com.insidesoftwares.jdempotent.core.generator.DefaultKeyGenerator;
import br.com.insidesoftwares.jdempotent.core.model.IdempotencyKey;
import br.com.insidesoftwares.jdempotent.core.model.IdempotentIgnorableWrapper;
import br.com.insidesoftwares.jdempotent.core.model.IdempotentRequestWrapper;
import br.com.insidesoftwares.jdempotent.core.utils.IdempotentTestPayload;
import br.com.insidesoftwares.jdempotent.core.utils.TestException;
import br.com.insidesoftwares.jdempotent.core.utils.TestIdempotentResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IdempotentAspectWithErrorCallbackITTest.class,
        TestAopWithErrorCallbackContext.class,
        TestIdempotentResource.class,
        DefaultKeyGenerator.class,
        InMemoryIdempotentRepository.class
})
class IdempotentAspectWithErrorCallbackITTest {

    @Autowired
    private TestIdempotentResource testIdempotentResource;

    @Autowired
    private InMemoryIdempotentRepository idempotentRepository;

    @Autowired
    private DefaultKeyGenerator defaultKeyGenerator;

    @Autowired
    private TestCustomErrorCallback testCustomErrorCallback;

    @Test
    void given_valid_payload_when_trigger_aspect_then_not_throw_custom_error_callback_and_save_repository() throws NoSuchAlgorithmException {
        //given
        IdempotentTestPayload test = new IdempotentTestPayload();
        test.setName("another");
        IdempotentIgnorableWrapper wrapper = new IdempotentIgnorableWrapper();
        wrapper.getNonIgnoredFields().put("name", "another");
        wrapper.getNonIgnoredFields().put("transactionId", null);
        IdempotencyKey idempotencyKey = defaultKeyGenerator.generateIdempotentKey(new IdempotentRequestWrapper(wrapper), "", new StringBuilder(), MessageDigest.getInstance(CryptographyAlgorithm.MD5.value()));

        //when
        testIdempotentResource.idempotentMethodReturnArg(test);

        //then
        assertTrue(idempotentRepository.contains(idempotencyKey));
    }

    @Test
    void given_invalid_payload_when_trigger_aspect_then_throw_test_exception_from_custom_error_callback_and_remove_repository() throws NoSuchAlgorithmException {
        //given
        IdempotentTestPayload test = new IdempotentTestPayload();
        test.setName("test");
        IdempotentIgnorableWrapper wrapper = new IdempotentIgnorableWrapper();
        wrapper.getNonIgnoredFields().put("name", "test");
        wrapper.getNonIgnoredFields().put("transactionId", null);
        IdempotencyKey idempotencyKey = defaultKeyGenerator.generateIdempotentKey(new IdempotentRequestWrapper(wrapper), "TestIdempotentResource", new StringBuilder(), MessageDigest.getInstance(CryptographyAlgorithm.MD5.value()));

        //when
        TestException testException = Assertions.assertThrows(
                TestException.class,
                () -> testIdempotentResource.idempotentMethodReturnArg(test)
        );

        //then
        assertEquals("Name will not be test", testException.getMessage());
        assertFalse(idempotentRepository.contains(idempotencyKey));
    }
}