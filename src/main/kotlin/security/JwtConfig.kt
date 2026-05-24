package security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {

    private const val SECRET = "my-super-secret-key"
    const val ISSUER = "workflow-app"
    const val AUDIENCE = "mobile-app"
    private const val VALIDITY = 7L * 24 * 60 * 60 * 1000

    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(SECRET))
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(userId: String, userType: String): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withClaim("userType", userType)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY))
            .sign(Algorithm.HMAC256(SECRET))
    }
}
