package com.github.porthos;

public interface Status {
    public static final int OK                          = 200;
    public static final int Created                     = 201;
    public static final int Accepted                    = 202;
    public static final int NonAuthoritativeInfo        = 203;
    public static final int NoContent                   = 204;
    public static final int ResetContent                = 205;
    public static final int PartialContent              = 206;
    public static final int MovedPermanently            = 301;
    public static final int Found                       = 302;
    public static final int NotModified                 = 304;
    public static final int BadRequest                  = 400;
    public static final int Unauthorized                = 401;
    public static final int Forbidden                   = 403;
    public static final int NotFound                    = 404;
    public static final int MethodNotAllowed            = 405;
    public static final int NotAcceptable               = 406;
    public static final int Conflict                    = 409;
    public static final int Gone                        = 410;
    public static final int Locked                      = 423;
    public static final int FailedDependency            = 424;
    public static final int PreconditionRequired        = 428;
    public static final int TooManyRequests             = 429;
    public static final int RequestHeaderFieldsTooLarge = 431;
    public static final int UnavailableForLegalReasons  = 451;
    public static final int InternalServerError         = 500;
    public static final int NotImplemented              = 501;
    public static final int ServiceUnavailable          = 503;
    public static final int InsufficientStorage         = 507;
}
