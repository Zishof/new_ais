package id.aisnext.config;

import id.aisnext.tenant.api.ResolvedTenant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Makes the trusted-host tenant resolved by the servlet filter available as a controller method
 * argument.
 */
@Configuration(proxyBeanMethods = false)
public class ResolvedTenantWebConfiguration implements WebMvcConfigurer {
    /**
     * Creates the MVC configuration that registers resolved-tenant argument handling.
     */
    public ResolvedTenantWebConfiguration() {
    }

    /**
     * Registers the resolver for controller parameters of type {@link ResolvedTenant}.
     *
     * @param resolvers mutable MVC resolver list supplied by Spring
     */
    @Override public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            /**
             * Reports whether a method parameter requests the tenant resolved for this request.
             *
             * @param parameter controller method parameter being inspected
             * @return {@code true} only for {@link ResolvedTenant}
             */
            @Override public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return parameter.getParameterType().equals(ResolvedTenant.class);
            }

            /**
             * Reads the trusted tenant object placed in request scope by the tenant filter.
             *
             * @param parameter supported controller parameter
             * @param container current model-and-view container
             * @param request current web request
             * @param binderFactory binder factory for the invocation
             * @return request-scoped tenant, or {@code null} if the filter contract was violated
             */
            @Override public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                    ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binderFactory) {
                return request.getAttribute("resolvedTenant", NativeWebRequest.SCOPE_REQUEST);
            }
        });
    }
}
