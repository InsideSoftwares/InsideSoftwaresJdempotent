package br.com.insidesoftwares.jdempotent.core.aspect;

import br.com.insidesoftwares.jdempotent.core.annotation.JdempotentResource;
import br.com.insidesoftwares.jdempotent.core.callback.ErrorConditionalCallback;
import br.com.insidesoftwares.jdempotent.core.datasource.IdempotentRepository;
import br.com.insidesoftwares.jdempotent.core.generator.DefaultKeyGenerator;
import br.com.insidesoftwares.jdempotent.core.model.IdempotencyKey;
import br.com.insidesoftwares.jdempotent.core.model.IdempotentIgnorableWrapper;
import br.com.insidesoftwares.jdempotent.core.utils.IdempotentTestPayload;
import br.com.insidesoftwares.jdempotent.core.utils.TestIdempotentResource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TestIdempotentResource.class,
        IdempotentAspectITTest.class,
        TestAopContext.class
})
class IdempotentAspectUTTest {

    @InjectMocks
    private IdempotentAspect idempotentAspect;

    @Mock
    private IdempotentRepository idempotentRepository;

    @Mock
    private DefaultKeyGenerator defaultKeyGenerator;

    @Mock
    private ErrorConditionalCallback errorCallback;

    @Test
    void given_new_payload_when_key_not_in_repository_and_method_has_one_arg_then_should_store_repository() throws Throwable {
        //given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestIdempotentResource.class.getMethod("idempotentMethod", IdempotentTestPayload.class);

        IdempotentTestPayload payload = new IdempotentTestPayload("payload");
        TestIdempotentResource testIdempotentResource = mock(TestIdempotentResource.class);

        when(defaultKeyGenerator.generateIdempotentKey(any(),any(),any(),any())).thenReturn(new IdempotencyKey("123"));
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(testIdempotentResource);
        when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TestIdempotentResource");
        when(idempotentRepository.contains(any())).thenReturn(false);

        //when
        idempotentAspect.execute(joinPoint);

        //then
        verify(joinPoint, times(4)).getSignature();
        verify(signature, times(3)).getMethod();
        verify(joinPoint).getTarget();
        verify(idempotentRepository, times(1)).store(any(), any(), any(), any());
        verify(joinPoint).proceed();
        verify(idempotentRepository, times(1)).setResponse(any(), any(), any(), any(), any());
    }

    @Test
    void given_actual_payload_when_key_in_repository_and_method_has_one_arg_then_should_not_store_repository() throws Throwable {
        //given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestIdempotentResource.class.getMethod("idempotentMethod", IdempotentTestPayload.class);
        JdempotentResource jdempotentResource = mock(JdempotentResource.class);
        IdempotentTestPayload payload = new IdempotentTestPayload("payload");
        TestIdempotentResource testIdempotentResource = mock(TestIdempotentResource.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(testIdempotentResource);
        when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TestIdempotentResource");
        when(idempotentRepository.contains(any())).thenReturn(true);

        //when
        idempotentAspect.execute(joinPoint);

        //then
        verify(joinPoint, times(4)).getSignature();
        verify(signature, times(3)).getMethod();
        verify(joinPoint).getTarget();
        verify(idempotentRepository, times(0)).store(any(), any());
        verify(joinPoint, times(0)).proceed();
        verify(idempotentRepository, times(0)).setResponse(any(), any(), any());
        verify(idempotentRepository, times(1)).getResponse(any());
    }

    @Test
    void given_actual_payload_when_key_in_repository_and_method_has_one_arg_then_should_store_repository_before_should_be_delete() throws Throwable {
        //given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestIdempotentResource.class.getMethod("idempotentMethodThrowingARuntimeException", IdempotentTestPayload.class);
        JdempotentResource jdempotentResource = mock(JdempotentResource.class);
        IdempotentTestPayload payload = new IdempotentTestPayload("payload");
        TestIdempotentResource testIdempotentResource = mock(TestIdempotentResource.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(testIdempotentResource);
        when(joinPoint.proceed()).thenThrow(new RuntimeException());
        when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TestIdempotentResource");
        when(idempotentRepository.contains(any())).thenReturn(false);

        //when
        Assertions.assertThrows(
                NullPointerException.class,
                () -> idempotentAspect.execute(joinPoint)
        );

        //then
        verify(joinPoint, times(4)).getSignature();
        verify(signature, times(3)).getMethod();
        verify(joinPoint).getTarget();
        verify(idempotentRepository, times(0)).store(any(), any());
        verify(joinPoint, times(0)).proceed();
        verify(idempotentRepository, times(0)).remove(any());
        verify(idempotentRepository, times(0)).setResponse(any(), any(), any());
        verify(idempotentRepository, times(0)).getResponse(any());
    }

    @Test
    void not_given_a_payload_then_return_exception() throws Throwable {
        //given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestIdempotentResource.class.getMethod("idempotentMethodWithZeroParamater");
        JdempotentResource jdempotentResource = mock(JdempotentResource.class);
        IdempotentTestPayload payload = new IdempotentTestPayload("payload");
        TestIdempotentResource testIdempotentResource = mock(TestIdempotentResource.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(testIdempotentResource);
        when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TestIdempotentResource");
        when(idempotentRepository.contains(any())).thenReturn(false);

        //when
        IllegalStateException illegalStateException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> idempotentAspect.execute(joinPoint)
        );

        //then
        assertEquals("Idempotent method not found", illegalStateException.getMessage());
        verify(joinPoint).getTarget();
        verify(joinPoint).getSignature();
        verify(signature, times(0)).getMethod();
        verify(idempotentRepository, times(0)).store(any(), any());
        verify(joinPoint, times(0)).proceed();
        verify(idempotentRepository, times(0)).setResponse(any(), any(), any());
    }

    @Test
    void given_a_payload_when_called_error_callback_then_should_return_exception() throws Throwable {
        //given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestIdempotentResource.class.getMethod("idempotentMethodThrowingARuntimeException", IdempotentTestPayload.class);
        IdempotentTestPayload payload = new IdempotentTestPayload("payload");
        TestIdempotentResource testIdempotentResource = mock(TestIdempotentResource.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(testIdempotentResource);
        when(joinPoint.proceed()).thenThrow(new RuntimeException());
        when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TestIdempotentResource");
        when(idempotentRepository.contains(any())).thenReturn(false);
        when(errorCallback.onErrorCondition(any())).thenReturn(true);
        when(errorCallback.onErrorCustomException()).thenReturn(new RuntimeException());

        Assertions.assertThrows(
                NullPointerException.class,
                () -> idempotentAspect.execute(joinPoint)
        );

        //then
        verify(joinPoint, times(4)).getSignature();
        verify(signature, times(3)).getMethod();
        verify(joinPoint).getTarget();
        verify(idempotentRepository, times(0)).store(any(), any());
        verify(joinPoint, times(0)).proceed();
        verify(idempotentRepository, times(0)).setResponse(any(), any(), any());
        verify(idempotentRepository, times(0)).remove(any());
    }

    @Test
    void given_a_payload_include_one_parameter_when_find_idempotent_request_then_return_idempotent_ignorable_wrapper() throws Throwable {
        //given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestIdempotentResource.class.getMethod("idempotentMethod", IdempotentTestPayload.class);

        IdempotentTestPayload payload = new IdempotentTestPayload("payload");
        TestIdempotentResource testIdempotentResource = mock(TestIdempotentResource.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(testIdempotentResource);
        when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TestIdempotentResource");
        when(idempotentRepository.contains(any())).thenReturn(false);

        //when
        var idempotentRequestWrapper = idempotentAspect.findIdempotentRequestArg(joinPoint);

        //then
        List<Object> requestWrapperRequests = idempotentRequestWrapper.getRequest();
        assertEquals(requestWrapperRequests.size(), 1);
        IdempotentIgnorableWrapper requestWrapperRequest = (IdempotentIgnorableWrapper) requestWrapperRequests.get(0);
        assertEquals(requestWrapperRequest.getNonIgnoredFields().size(), 2);
        assertEquals(requestWrapperRequest.getNonIgnoredFields().get("name"), "payload");
        assertNull(requestWrapperRequest.getNonIgnoredFields().get("transactionId"));
        verify(joinPoint).getArgs();
    }

    @Test
    void given_a_payload_with_jdempotent_property_when_find_idempotent_request_then_return_idempotent_ignorable_wrapper() throws Throwable {
        //given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestIdempotentResource.class.getMethod("idempotentMethod", IdempotentTestPayload.class);

        IdempotentTestPayload payload = new IdempotentTestPayload("payload");
        payload.setEventId(1l);
        TestIdempotentResource testIdempotentResource = mock(TestIdempotentResource.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{payload});
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(testIdempotentResource);
        when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TestIdempotentResource");
        when(idempotentRepository.contains(any())).thenReturn(false);

        //when
        var idempotentRequestWrapper = idempotentAspect.findIdempotentRequestArg(joinPoint);

        //then
        List<Object> requestWrapperRequests = idempotentRequestWrapper.getRequest();
        assertEquals(requestWrapperRequests.size(), 1);
        IdempotentIgnorableWrapper requestWrapperRequest = (IdempotentIgnorableWrapper) requestWrapperRequests.get(0);
        assertEquals(requestWrapperRequest.getNonIgnoredFields().size(), 2);
        assertEquals(requestWrapperRequest.getNonIgnoredFields().get("name"), "payload");
        assertEquals(requestWrapperRequest.getNonIgnoredFields().get("transactionId"), 1l);
        verify(joinPoint).getArgs();
    }
}
