# Keep classes which may be lost by Proguard or R8 when using Answer Bot

-keep class zendesk.core.AuthenticationRequestWrapper { *; }
-keep class zendesk.core.PushRegistrationRequestWrapper { *; }
-keep class zendesk.core.PushRegistrationRequest { *; }
-keep class zendesk.core.PushRegistrationResponse { *; }
-keep class zendesk.core.ApiAnonymousIdentity { *; }

-keep class zendesk.support.CreateRequestWrapper { *; }
-keep class zendesk.support.Comment { *; }

-keep class zendesk.answerbot.Deflection { *; }
-keep class zendesk.answerbot.AnswerBotSettings { *; }
