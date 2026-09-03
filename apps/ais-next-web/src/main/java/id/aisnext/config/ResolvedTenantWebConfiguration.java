package id.aisnext.config;

import id.aisnext.tenant.api.ResolvedTenant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class ResolvedTenantWebConfiguration implements WebMvcConfigurer {
    @Override public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return parameter.getParameterType().equals(ResolvedTenant.class);
            }
            @Override public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                    ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binderFactory) {
                return request.getAttribute("resolvedTenant", NativeWebRequest.SCOPE_REQUEST);
            }
        });
    }
}
