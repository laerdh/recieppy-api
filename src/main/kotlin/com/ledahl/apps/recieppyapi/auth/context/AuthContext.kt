package com.ledahl.apps.recieppyapi.auth.context

import com.ledahl.apps.recieppyapi.model.User
import graphql.servlet.GraphQLContext
import javax.servlet.http.HttpServletRequest

class AuthContext(val request: HttpServletRequest?,
                  val user: User): GraphQLContext(request)