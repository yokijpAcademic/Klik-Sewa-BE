package com.gity.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.swagger.codegen.v3.generators.html.StaticCodegen

fun Application.configureOpenAPI() {
    install(OpenAPI) {
        info {
            version = "1.0.0"
            title = "Klik Sewa API"
            description = "Backend API untuk aplikasi Klik Sewa - Platform penyewaan barang"
            contact {
                name = "API Support"
                email = "support@kliksewa.com"
            }
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
        components {
            securitySchemes {
                bearerAuth {
                    type = SecuritySchemeType.HTTP
                    scheme = "bearer"
                    bearerFormat = "JWT"
                    description = "JWT Authentication token"
                }
            }
        }
        security {
            addSecurity("bearerAuth", emptyList())
        }
    }

    routing {
        route("/api-docs") {
            get {
                call.respond(
                    HttpStatusCode.OK,
                    application.openAPIGen.api
                )
            }
        }

        // Swagger UI
        get("/swagger-ui") {
            call.respondText(
                StaticCodegen().render(
                    application.openAPIGen.api,
                    "Klik Sewa API Documentation",
                    "/api-docs"
                ),
                ContentType.Text.Html
            )
        }
    }
}