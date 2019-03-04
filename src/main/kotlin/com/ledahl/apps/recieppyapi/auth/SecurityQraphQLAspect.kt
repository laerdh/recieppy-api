package com.ledahl.apps.recieppyapi.auth

import com.ledahl.apps.recieppyapi.model.User
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED

/**
 * https://mi3o.com/spring-graphql-security/
 */
@Aspect
@Component
@Order(1)
class SecurityQraphQLAspect {

    /**
     * All graphQLResolver methods can be called only by authenticated user.
     * @Unsecured annotated methods are excluded
     */
    @Before("allGraphQLResolverMethods() && isDefinedInApplication() && !isMethodAnnotatedAsUnsecured()")
    fun doSecurityCheck() {
        val requestAttributes = RequestContextHolder.currentRequestAttributes() as? ServletRequestAttributes
        val response = requestAttributes?.response

        val user: User? = requestAttributes?.getAttribute("USER", RequestAttributes.SCOPE_REQUEST) as? User
        if (user == null) {
            response?.sendError(SC_UNAUTHORIZED, "Invalid token")
        }
    }

    /**
     * Matches all beans that implement [com.coxautodev.graphql.tools.GraphQLResolver]
     * note: `GraphQLMutationResolver`, `GraphQLQueryResolver` etc
     * extend base GraphQLResolver interface
     */
    @Pointcut("target(com.coxautodev.graphql.tools.GraphQLResolver)")
    private fun allGraphQLResolverMethods() {
    }

    /**
     * Matches all beans in com.ledahl.apps.recieppyapi package
     * resolvers must be in this package (subpackages)
     */
    @Pointcut("within(com.ledahl.apps.recieppyapi..*)")
    private fun isDefinedInApplication() {
    }

    /**
     * Any method annotated with @Unsecured
     */
    @Pointcut("@annotation(com.ledahl.apps.recieppyapi.auth.Unsecured)")
    private fun isMethodAnnotatedAsUnsecured() {
    }
}
