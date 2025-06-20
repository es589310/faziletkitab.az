/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "tcn.h"
#include "apr_file_io.h"
#include "apr_thread_mutex.h"
#include "apr_atomic.h"
#include "apr_poll.h"

#ifdef HAVE_OPENSSL
#include "ssl_private.h"

static int ssl_initialized = 0;
static char *ssl_global_rand_file = NULL;
extern apr_pool_t *tcn_global_pool;

ENGINE *tcn_ssl_engine = NULL;
tcn_pass_cb_t tcn_password_callback;

#ifdef HAVE_KEYLOG_CALLBACK
static BIO *key_log_file = NULL;

static void ssl_keylog_callback(const SSL *ssl, const char *line)
{
    if (key_log_file && line && *line) {
        BIO_puts(key_log_file, line);
        BIO_puts(key_log_file, "\n");
    }
}
#endif

/* From netty-tcnative */
static jclass byteArrayClass;
static jclass stringClass;

#if OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)
/* Global reference to the pool used by the dynamic mutexes */
static apr_pool_t *dynlockpool = NULL;

/* Dynamic lock structure */
struct CRYPTO_dynlock_value {
    apr_pool_t *pool;
    const char* file;
    int line;
    apr_thread_mutex_t *mutex;
};

#if ! (defined(WIN32) || defined(WIN64))
apr_threadkey_t *thread_exit_key;
static int threadkey_initialized = 0;
#endif
#endif

/*
 * supported_ssl_opts is a bitmask that contains all supported SSL_OP_*
 * options at compile-time. This is used in hasOp to determine which
 * SSL_OP_* options are available at runtime.
 *
 * Note that at least up through OpenSSL 0.9.8o, checking SSL_OP_ALL will
 * return JNI_FALSE because SSL_OP_ALL is a mask that covers all bug
 * workarounds for OpenSSL including future workarounds that are defined
 * to be in the least-significant 3 nibbles of the SSL_OP_* bit space.
 *
 * This implementation has chosen NOT to simply set all those lower bits
 * so that the return value for SSL_OP_FUTURE_WORKAROUND will only be
 * reported by versions that actually support that specific workaround.
 */
static const jint supported_ssl_opts = 0
/*
  Specifically skip SSL_OP_ALL
#ifdef SSL_OP_ALL
     | SSL_OP_ALL
#endif
*/
#ifdef SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION
     | SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION
#endif

#ifdef SSL_OP_CIPHER_SERVER_PREFERENCE
     | SSL_OP_CIPHER_SERVER_PREFERENCE
#endif

#ifdef SSL_OP_CRYPTOPRO_TLSEXT_BUG
     | SSL_OP_CRYPTOPRO_TLSEXT_BUG
#endif

#ifdef SSL_OP_DONT_INSERT_EMPTY_FRAGMENTS
     | SSL_OP_DONT_INSERT_EMPTY_FRAGMENTS
#endif

#ifdef SSL_OP_EPHEMERAL_RSA
     | SSL_OP_EPHEMERAL_RSA
#endif

#ifdef SSL_OP_LEGACY_SERVER_CONNECT
     | SSL_OP_LEGACY_SERVER_CONNECT
#endif

#ifdef SSL_OP_MICROSOFT_BIG_SSLV3_BUFFER
     | SSL_OP_MICROSOFT_BIG_SSLV3_BUFFER
#endif

#ifdef SSL_OP_MICROSOFT_SESS_ID_BUG
     | SSL_OP_MICROSOFT_SESS_ID_BUG
#endif

#ifdef SSL_OP_MSIE_SSLV2_RSA_PADDING
     | SSL_OP_MSIE_SSLV2_RSA_PADDING
#endif

#ifdef SSL_OP_NETSCAPE_CA_DN_BUG
     | SSL_OP_NETSCAPE_CA_DN_BUG
#endif

#ifdef SSL_OP_NETSCAPE_CHALLENGE_BUG
     | SSL_OP_NETSCAPE_CHALLENGE_BUG
#endif

#ifdef SSL_OP_NETSCAPE_DEMO_CIPHER_CHANGE_BUG
     | SSL_OP_NETSCAPE_DEMO_CIPHER_CHANGE_BUG
#endif

#ifdef SSL_OP_NETSCAPE_REUSE_CIPHER_CHANGE_BUG
     | SSL_OP_NETSCAPE_REUSE_CIPHER_CHANGE_BUG
#endif

#ifdef SSL_OP_NO_COMPRESSION
     | SSL_OP_NO_COMPRESSION
#endif

#ifdef SSL_OP_NO_QUERY_MTU
     | SSL_OP_NO_QUERY_MTU
#endif

#ifdef SSL_OP_NO_SESSION_RESUMPTION_ON_RENEGOTIATION
     | SSL_OP_NO_SESSION_RESUMPTION_ON_RENEGOTIATION
#endif

#ifdef SSL_OP_NO_SSLv2
     | SSL_OP_NO_SSLv2
#endif

#ifdef SSL_OP_NO_SSLv3
     | SSL_OP_NO_SSLv3
#endif

#ifdef SSL_OP_NO_TICKET
     | SSL_OP_NO_TICKET
#endif

#ifdef SSL_OP_NO_TLSv1
     | SSL_OP_NO_TLSv1
#endif

#ifdef SSL_OP_PKCS1_CHECK_1
     | SSL_OP_PKCS1_CHECK_1
#endif

#ifdef SSL_OP_PKCS1_CHECK_2
     | SSL_OP_PKCS1_CHECK_2
#endif

#ifdef SSL_OP_NO_TLSv1_1
     | SSL_OP_NO_TLSv1_1
#endif

#ifdef SSL_OP_NO_TLSv1_2
     | SSL_OP_NO_TLSv1_2
#endif

#ifdef SSL_OP_SINGLE_DH_USE
     | SSL_OP_SINGLE_DH_USE
#endif

#ifdef SSL_OP_SINGLE_ECDH_USE
     | SSL_OP_SINGLE_ECDH_USE
#endif

#ifdef SSL_OP_SSLEAY_080_CLIENT_DH_BUG
     | SSL_OP_SSLEAY_080_CLIENT_DH_BUG
#endif

#ifdef SSL_OP_SSLREF2_REUSE_CERT_TYPE_BUG
     | SSL_OP_SSLREF2_REUSE_CERT_TYPE_BUG
#endif

#ifdef SSL_OP_TLS_BLOCK_PADDING_BUG
     | SSL_OP_TLS_BLOCK_PADDING_BUG
#endif

#ifdef SSL_OP_TLS_D5_BUG
     | SSL_OP_TLS_D5_BUG
#endif

#ifdef SSL_OP_TLS_ROLLBACK_BUG
     | SSL_OP_TLS_ROLLBACK_BUG
#endif
     | 0;

#if OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)
/* OpenSSL Pre-1.1.0 compatibility */
/* Taken from OpenSSL 1.1.0 snapshot 20160410 */
int DH_set0_pqg(DH *dh, BIGNUM *p, BIGNUM *q, BIGNUM *g)
{
    /* q is optional */
    if (p == NULL || g == NULL)
        return 0;
    BN_free(dh->p);
    BN_free(dh->q);
    BN_free(dh->g);
    dh->p = p;
    dh->q = q;
    dh->g = g;

    if (q != NULL) {
        dh->length = BN_num_bits(q);
    }

    return 1;
}
#endif

/*
 * Grab well-defined DH parameters from OpenSSL, see the BN_get_rfc*
 * functions in <openssl/bn.h> for all available primes.
 */
static DH *make_dh_params(BIGNUM *(*prime)(BIGNUM *))
{
    DH *dh = DH_new();
    BIGNUM *p, *g;

    if (!dh) {
        return NULL;
    }
    p = prime(NULL);
    g = BN_new();
    if (g != NULL) {
        BN_set_word(g, 2);
    }
    if (!p || !g || !DH_set0_pqg(dh, p, NULL, g)) {
        DH_free(dh);
        BN_free(p);
        BN_free(g);
        return NULL;
    }
    return dh;
}

/* Storage and initialization for DH parameters. */
static struct dhparam {
    BIGNUM *(*const prime)(BIGNUM *); /* function to generate... */
    DH *dh;                           /* ...this, used for keys.... */
    const unsigned int min;           /* ...of length >= this. */
} dhparams[] = {
    { BN_get_rfc3526_prime_8192, NULL, 6145 },
    { BN_get_rfc3526_prime_6144, NULL, 4097 },
    { BN_get_rfc3526_prime_4096, NULL, 3073 },
    { BN_get_rfc3526_prime_3072, NULL, 2049 },
    { BN_get_rfc3526_prime_2048, NULL, 1025 },
    { BN_get_rfc2409_prime_1024, NULL, 0 }
};

static void init_dh_params(void)
{
    unsigned n;

    for (n = 0; n < sizeof(dhparams)/sizeof(dhparams[0]); n++)
        dhparams[n].dh = make_dh_params(dhparams[n].prime);
}

static void free_dh_params(void)
{
    unsigned n;

    /* DH_free() is a noop for a NULL parameter, so these are harmless
     * in the (unexpected) case where these variables are already
     * NULL. */
    for (n = 0; n < sizeof(dhparams)/sizeof(dhparams[0]); n++) {
        DH_free(dhparams[n].dh);
        dhparams[n].dh = NULL;
    }
}

#ifdef HAVE_KEYLOG_CALLBACK
void SSL_callback_add_keylog(SSL_CTX *ctx)
{
    if (key_log_file) {
        SSL_CTX_set_keylog_callback(ctx, ssl_keylog_callback);
    }
}
#endif

/* Hand out the same DH structure though once generated as we leak
 * memory otherwise and freeing the structure up after use would be
 * hard to track and in fact is not needed at all as it is safe to
 * use the same parameters over and over again security wise (in
 * contrast to the keys itself) and code safe as the returned structure
 * is duplicated by OpenSSL anyway. Hence no modification happens
 * to our copy. */
DH *SSL_get_dh_params(unsigned keylen)
{
    unsigned n;

    for (n = 0; n < sizeof(dhparams)/sizeof(dhparams[0]); n++)
        if (keylen >= dhparams[n].min)
            return dhparams[n].dh;

    return NULL; /* impossible to reach. */
}

#if OPENSSL_VERSION_NUMBER >= 0x10100000L && !defined(LIBRESSL_VERSION_NUMBER)
static void init_bio_methods(void);
static void free_bio_methods(void);
#endif

TCN_IMPLEMENT_CALL(jint, SSL, version)(TCN_STDARGS)
{
    UNREFERENCED_STDARGS;
    return OpenSSL_version_num();
}

TCN_IMPLEMENT_CALL(jstring, SSL, versionString)(TCN_STDARGS)
{
    UNREFERENCED(o);
    return AJP_TO_JSTRING(OpenSSL_version(OPENSSL_VERSION));
}

/*
 *  the various processing hooks
 */
static apr_status_t ssl_init_cleanup(void *data)
{
    UNREFERENCED(data);

    if (!ssl_initialized)
        return APR_SUCCESS;
    ssl_initialized = 0;

#if (OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)) && ! (defined(WIN32) || defined(WIN64))
    if (threadkey_initialized) {
        threadkey_initialized = 0;
        apr_threadkey_private_delete(thread_exit_key);
    }
#endif
    if (tcn_password_callback.cb.obj) {
        JNIEnv *env;
        tcn_get_java_env(&env);
        TCN_UNLOAD_CLASS(env,
                         tcn_password_callback.cb.obj);
    }

#if OPENSSL_VERSION_NUMBER >= 0x10100000L && !defined(LIBRESSL_VERSION_NUMBER)
    free_bio_methods();
#endif
    free_dh_params();

#ifndef OPENSSL_NO_ENGINE
    if (tcn_ssl_engine != NULL) {
        /* Release the SSL Engine structural reference */
        ENGINE_free(tcn_ssl_engine);
        tcn_ssl_engine = NULL;
    }
#endif

#if OPENSSL_VERSION_NUMBER >= 0x10100000L && !defined(LIBRESSL_VERSION_NUMBER)
    /* Openssl v1.1+ handles all termination automatically. Do
     * nothing in this case.
     */
#else
    /*
     * Try to kill the internals of the SSL library.
     */
#ifdef OPENSSL_FIPS
    FIPS_mode_set(0);
#endif
    /* Corresponds to OPENSSL_load_builtin_modules() */
    CONF_modules_free();
    /* Corresponds to SSL_library_init: */
    EVP_cleanup();
#if HAVE_ENGINE_LOAD_BUILTIN_ENGINES
    ENGINE_cleanup();
#endif
#ifndef OPENSSL_NO_COMP
    SSL_COMP_free_compression_methods();
#endif
    CRYPTO_cleanup_all_ex_data();
#if OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)
    ERR_remove_thread_state(NULL);
#endif
#endif

#ifdef HAVE_KEYLOG_CALLBACK
    if (key_log_file) {
        BIO_free(key_log_file);
        key_log_file = NULL;
    }
#endif

    /* Don't call ERR_free_strings here; ERR_load_*_strings only
     * actually load the error strings once per process due to static
     * variable abuse in OpenSSL. */

    /*
     * TODO: determine somewhere we can safely shove out diagnostics
     *       (when enabled) at this late stage in the game:
     * CRYPTO_mem_leaks_fp(stderr);
     */
    return APR_SUCCESS;
}

#ifndef OPENSSL_NO_ENGINE
/* Try to load an engine in a shareable library */
static ENGINE *ssl_try_load_engine(const char *engine)
{
    ENGINE *e = ENGINE_by_id("dynamic");
    if (e) {
        if (!ENGINE_ctrl_cmd_string(e, "SO_PATH", engine, 0)
            || !ENGINE_ctrl_cmd_string(e, "LOAD", NULL, 0)) {
            ENGINE_free(e);
            e = NULL;
        }
    }
    return e;
}
#endif

/*
 * To ensure thread-safetyness in OpenSSL
 */

#if OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)
static apr_thread_mutex_t **ssl_lock_cs;
static int                  ssl_lock_num_locks;

static void ssl_thread_lock(int mode, int type,
                            const char *file, int line)
{
    UNREFERENCED(file);
    UNREFERENCED(line);
    if (type < ssl_lock_num_locks) {
        if (mode & CRYPTO_LOCK) {
            apr_thread_mutex_lock(ssl_lock_cs[type]);
        }
        else {
            apr_thread_mutex_unlock(ssl_lock_cs[type]);
        }
    }
}
#endif

static unsigned long ssl_thread_id(void)
{
    return (unsigned long)tcn_get_thread_id();
}

#if OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)
#if ! (defined(WIN32) || defined(WIN64))
void SSL_thread_exit(void) {
    ERR_remove_thread_state(NULL);
    apr_threadkey_private_set(NULL, thread_exit_key);
}

unsigned long SSL_ERR_get() {
    apr_threadkey_private_set(thread_exit_key, thread_exit_key);
    return ERR_get_error();
}

void SSL_ERR_clear() {
    apr_threadkey_private_set(thread_exit_key, thread_exit_key);
    ERR_clear_error();
}

static void _ssl_thread_exit(void *data) {
    UNREFERENCED(data);
    SSL_thread_exit();
}
#endif

static void ssl_set_thread_id(CRYPTO_THREADID *id)
{
    CRYPTO_THREADID_set_numeric(id, ssl_thread_id());
}

static apr_status_t ssl_thread_cleanup(void *data)
{
    UNREFERENCED(data);
    CRYPTO_THREADID_set_callback(NULL);
    CRYPTO_set_locking_callback(NULL);
    CRYPTO_set_dynlock_create_callback(NULL);
    CRYPTO_set_dynlock_lock_callback(NULL);
    CRYPTO_set_dynlock_destroy_callback(NULL);

    dynlockpool = NULL;

    /* Let the registered mutex cleanups do their own thing
     */
    return APR_SUCCESS;
}

/*
 * Dynamic lock creation callback
 */
static struct CRYPTO_dynlock_value *ssl_dyn_create_function(const char *file,
                                                     int line)
{
    struct CRYPTO_dynlock_value *value;
    apr_pool_t *p;
    apr_status_t rv;

    /*
     * We need a pool to allocate our mutex.  Since we can't clear
     * allocated memory from a pool, create a subpool that we can blow
     * away in the destruction callback.
     */
    rv = apr_pool_create(&p, dynlockpool);
    if (rv != APR_SUCCESS) {
        /* TODO log that fprintf(stderr, "Failed to create subpool for dynamic lock"); */
        return NULL;
    }

    value = (struct CRYPTO_dynlock_value *)apr_palloc(p,
                                                      sizeof(struct CRYPTO_dynlock_value));
    if (!value) {
        /* TODO log that fprintf(stderr, "Failed to allocate dynamic lock structure"); */
        return NULL;
    }

    value->pool = p;
    /* Keep our own copy of the place from which we were created,
       using our own pool. */
    value->file = apr_pstrdup(p, file);
    value->line = line;
    rv = apr_thread_mutex_create(&(value->mutex), APR_THREAD_MUTEX_DEFAULT,
                                p);
    if (rv != APR_SUCCESS) {
        /* TODO log that fprintf(stderr, "Failed to create thread mutex for dynamic lock"); */
        apr_pool_destroy(p);
        return NULL;
    }
    return value;
}

/*
 * Dynamic locking and unlocking function
 */

static void ssl_dyn_lock_function(int mode, struct CRYPTO_dynlock_value *l,
                           const char *file, int line)
{


    if (mode & CRYPTO_LOCK) {
        apr_thread_mutex_lock(l->mutex);
    }
    else {
        apr_thread_mutex_unlock(l->mutex);
    }
}

/*
 * Dynamic lock destruction callback
 */
static void ssl_dyn_destroy_function(struct CRYPTO_dynlock_value *l,
                          const char *file, int line)
{
    apr_status_t rv;
    rv = apr_thread_mutex_destroy(l->mutex);
    if (rv != APR_SUCCESS) {
        /* TODO log that fprintf(stderr, "Failed to destroy mutex for dynamic lock %s:%d", l->file, l->line); */
    }
    /* Trust that whomever owned the CRYPTO_dynlock_value we were
     * passed has no future use for it...
     */
    apr_pool_destroy(l->pool);
}

static void ssl_thread_setup(apr_pool_t *p)
{
    int i;

    CRYPTO_THREADID_set_callback(ssl_set_thread_id);
    ssl_lock_num_locks = CRYPTO_num_locks();
    ssl_lock_cs = apr_palloc(p, ssl_lock_num_locks * sizeof(*ssl_lock_cs));

    for (i = 0; i < ssl_lock_num_locks; i++) {
        apr_thread_mutex_create(&(ssl_lock_cs[i]),
                                APR_THREAD_MUTEX_DEFAULT, p);
    }

    CRYPTO_set_locking_callback(ssl_thread_lock);
    /* Set up dynamic locking scaffolding for OpenSSL to use at its
     * convenience.
     */
    dynlockpool = p;
    CRYPTO_set_dynlock_create_callback(ssl_dyn_create_function);
    CRYPTO_set_dynlock_lock_callback(ssl_dyn_lock_function);
    CRYPTO_set_dynlock_destroy_callback(ssl_dyn_destroy_function);

    apr_pool_cleanup_register(p, NULL, ssl_thread_cleanup,
                              apr_pool_cleanup_null);
}
#endif

static int ssl_rand_choosenum(int l, int h)
{
    int i;
    char buf[50];

    apr_snprintf(buf, sizeof(buf), "%.0f",
                 (((double)(rand()%RAND_MAX)/RAND_MAX)*(h-l)));
    i = atoi(buf)+1;
    if (i < l) i = l;
    if (i > h) i = h;
    return i;
}

static int ssl_rand_load_file(const char *file)
{
    char buffer[APR_PATH_MAX];
    int n;

    if (file == NULL)
        file = ssl_global_rand_file;
    if (file && (strcmp(file, "builtin") == 0))
        return -1;
    if (file == NULL)
        file = RAND_file_name(buffer, sizeof(buffer));
    if (file) {
        if (strncmp(file, "egd:", 4) == 0) {
#ifndef OPENSSL_NO_EGD
            if ((n = RAND_egd(file + 4)) > 0)
                return n;
            else
#endif
                return -1;
        }
        if ((n = RAND_load_file(file, -1)) > 0)
            return n;
    }
    return -1;
}

/*
 * writes a number of random bytes (currently 1024) to
 * file which can be used to initialize the PRNG by calling
 * RAND_load_file() in a later session
 */
static int ssl_rand_save_file(const char *file)
{
    char buffer[APR_PATH_MAX];
#ifndef OPENSSL_NO_EGD
    int n;
#endif

    if (file == NULL)
        file = RAND_file_name(buffer, sizeof(buffer));
#ifndef OPENSSL_NO_EGD
    else if ((n = RAND_egd(file)) > 0) {
        return 0;
    }
#endif
    if (file == NULL || !RAND_write_file(file))
        return 0;
    else
        return 1;
}

int SSL_rand_seed(const char *file)
{
    unsigned char stackdata[256];
    static volatile apr_uint32_t counter = 0;

    if (ssl_rand_load_file(file) < 0) {
        int n;
        struct {
            apr_time_t    t;
            pid_t         p;
            unsigned long i;
            apr_uint32_t  u;
        } _ssl_seed;
        if (counter == 0) {
            apr_generate_random_bytes(stackdata, 256);
            RAND_seed(stackdata, 128);
        }
        _ssl_seed.t = apr_time_now();
        _ssl_seed.p = getpid();
        _ssl_seed.i = ssl_thread_id();
        apr_atomic_inc32(&counter);
        _ssl_seed.u = counter;
        RAND_seed((unsigned char *)&_ssl_seed, sizeof(_ssl_seed));
        /*
         * seed in some current state of the run-time stack (128 bytes)
         */
        n = ssl_rand_choosenum(0, sizeof(stackdata)-128-1);
        RAND_seed(stackdata + n, 128);
    }
    return RAND_status();
}

static int ssl_rand_make(const char *file, int len, int base64)
{
    int r;
    int num = len;
    BIO *out = NULL;

    out = BIO_new(BIO_s_file());
    if (out == NULL)
        return 0;
    if ((r = BIO_write_filename(out, (char *)file)) < 0) {
        BIO_free_all(out);
        return 0;
    }
    if (base64) {
        BIO *b64 = BIO_new(BIO_f_base64());
        if (b64 == NULL) {
            BIO_free_all(out);
            return 0;
        }
        out = BIO_push(b64, out);
    }
    while (num > 0) {
        unsigned char buf[4096];
        int len = num;
        if (len > sizeof(buf))
            len = sizeof(buf);
        r = RAND_bytes(buf, len);
        if (r <= 0) {
            BIO_free_all(out);
            return 0;
        }
        BIO_write(out, buf, len);
        num -= len;
    }
    r = BIO_flush(out);
    BIO_free_all(out);
    return r > 0 ? 1 : 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, initialize)(TCN_STDARGS, jstring engine)
{
    jclass clazz;
    jclass sClazz;
#if !defined(OPENSSL_NO_ENGINE) || OPENSSL_VERSION_NUMBER < 0x10100000L
    apr_status_t err = APR_SUCCESS;
#endif

    TCN_ALLOC_CSTRING(engine);

    UNREFERENCED(o);
    if (!tcn_global_pool) {
        TCN_FREE_CSTRING(engine);
        tcn_ThrowAPRException(e, APR_EINVAL);
        return (jint)APR_EINVAL;
    }
    /* Check if already initialized */
    if (ssl_initialized++) {
        TCN_FREE_CSTRING(engine);
        return (jint)APR_SUCCESS;
    }
#if OPENSSL_VERSION_NUMBER >= 0x10100000L && !defined(LIBRESSL_VERSION_NUMBER)
    /* Openssl v1.1+ handles all initialisation automatically, apart
     * from hints as to how we want to use the library.
     *
     * We tell openssl we want to include engine support.
     */
    OPENSSL_init_ssl(OPENSSL_INIT_ENGINE_ALL_BUILTIN, NULL);
#else
    /* We must register the library in full, to ensure our configuration
     * code can successfully test the SSL environment.
     */
    OPENSSL_malloc_init();
    ERR_load_crypto_strings();
    SSL_load_error_strings();
    SSL_library_init();
    OpenSSL_add_all_algorithms();
#if HAVE_ENGINE_LOAD_BUILTIN_ENGINES
    ENGINE_load_builtin_engines();
#endif
    OPENSSL_load_builtin_modules();

#if ! (defined(WIN32) || defined(WIN64))
    err = apr_threadkey_private_create(&thread_exit_key, _ssl_thread_exit,
                                       tcn_global_pool);
    if (err != APR_SUCCESS) {
        ssl_init_cleanup(NULL);
        tcn_ThrowAPRException(e, err);
        return (jint)err;
    }
    threadkey_initialized = 1;
#endif
    /* Initialize thread support */
    ssl_thread_setup(tcn_global_pool);
#endif

#ifndef OPENSSL_NO_ENGINE
    if (J2S(engine)) {
        ENGINE *ee = NULL;
        if(strcmp(J2S(engine), "auto") == 0) {
            ENGINE_register_all_complete();
        }
        else {
            if ((ee = ENGINE_by_id(J2S(engine))) == NULL
                && (ee = ssl_try_load_engine(J2S(engine))) == NULL)
                err = APR_ENOTIMPL;
            else {
#ifdef ENGINE_CTRL_CHIL_SET_FORKCHECK
            	if (strcmp(J2S(engine), "chil") == 0)
                    ENGINE_ctrl(ee, ENGINE_CTRL_CHIL_SET_FORKCHECK, 1, 0, 0);
#endif
                if (!ENGINE_set_default(ee, ENGINE_METHOD_ALL))
                    err = APR_ENOTIMPL;
            }
        }
        if (err != APR_SUCCESS) {
            TCN_FREE_CSTRING(engine);
            ssl_init_cleanup(NULL);
            tcn_ThrowAPRException(e, err);
            return (jint)err;
        }
        tcn_ssl_engine = ee;
    }
#endif

    memset(&tcn_password_callback, 0, sizeof(tcn_pass_cb_t));
    /* Initialize PRNG
     * This will in most cases call the builtin
     * low entropy seed.
     */
    SSL_rand_seed(NULL);
    /* For SSL_get_app_data2(), SSL_get_app_data3() and SSL_get_app_data4() at request time */
    SSL_init_app_data_idx();

    init_dh_params();
#if OPENSSL_VERSION_NUMBER >= 0x10100000L && !defined(LIBRESSL_VERSION_NUMBER)
    init_bio_methods();
#endif

    /*
     * Let us cleanup the ssl library when the library is unloaded
     */
    apr_pool_cleanup_register(tcn_global_pool, NULL,
                              ssl_init_cleanup,
                              apr_pool_cleanup_null);
    TCN_FREE_CSTRING(engine);

    /* Cache the byte[].class for performance reasons */
    clazz = (*e)->FindClass(e, "[B");
    byteArrayClass = (jclass) (*e)->NewGlobalRef(e, clazz);

    /* Cache the String.class for performance reasons */
    sClazz = (*e)->FindClass(e, "java/lang/String");
    stringClass = (jclass) (*e)->NewGlobalRef(e, sClazz);

#ifdef HAVE_KEYLOG_CALLBACK
    if (!key_log_file) {
        char *key_log_file_name = getenv("SSLKEYLOGFILE");
        if (key_log_file_name) {
            FILE *file = fopen(key_log_file_name, "a");
            if (file) {
                if (setvbuf(file, NULL, _IONBF, 0)) {
                    fclose(file);
                } else {
                    key_log_file = BIO_new_fp(file, BIO_CLOSE);
                }
            }
        }
    }
#endif

    return (jint)APR_SUCCESS;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randLoad)(TCN_STDARGS, jstring file)
{
    TCN_ALLOC_CSTRING(file);
    int r;
    UNREFERENCED(o);
    r = SSL_rand_seed(J2S(file));
    TCN_FREE_CSTRING(file);
    return r ? JNI_TRUE : JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randSave)(TCN_STDARGS, jstring file)
{
    TCN_ALLOC_CSTRING(file);
    int r;
    UNREFERENCED(o);
    r = ssl_rand_save_file(J2S(file));
    TCN_FREE_CSTRING(file);
    return r ? JNI_TRUE : JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randMake)(TCN_STDARGS, jstring file,
                                            jint length, jboolean base64)
{
    TCN_ALLOC_CSTRING(file);
    int r;
    UNREFERENCED(o);
    r = ssl_rand_make(J2S(file), length, base64);
    TCN_FREE_CSTRING(file);
    return r ? JNI_TRUE : JNI_FALSE;
}

TCN_IMPLEMENT_CALL(void, SSL, randSet)(TCN_STDARGS, jstring file)
{
    TCN_ALLOC_CSTRING(file);
    UNREFERENCED(o);
    if (J2S(file)) {
        ssl_global_rand_file = apr_pstrdup(tcn_global_pool, J2S(file));
    }
    TCN_FREE_CSTRING(file);
}

TCN_IMPLEMENT_CALL(jint, SSL, fipsModeGet)(TCN_STDARGS)
{
    UNREFERENCED(o);
#ifdef OPENSSL_FIPS
    return FIPS_mode();
#elif (OPENSSL_VERSION_NUMBER > 0x2FFFFFFFL)
    EVP_MD              *md;
    const OSSL_PROVIDER *provider;
    const char          *name;

    // Maps the OpenSSL 3. x onwards behaviour to theOpenSSL 1.x API

    // Checks that FIPS is the default provider
    md = EVP_MD_fetch(NULL, "SHA-512", NULL);
    provider = EVP_MD_get0_provider(md);
    name = OSSL_PROVIDER_get0_name(provider);
    // Clean up
    EVP_MD_free(md);

    if (strcmp("fips", name)) {
        return 0;
    } else {
    	return 1;
    }
#else
    /* FIPS is unavailable */
    tcn_ThrowException(e, "FIPS was not available to tcnative at build time. You will need to re-build tcnative against an OpenSSL with FIPS.");

    return 0;
#endif
}

TCN_IMPLEMENT_CALL(jint, SSL, fipsModeSet)(TCN_STDARGS, jint mode)
{
    int r = 0;
    UNREFERENCED(o);

#ifdef OPENSSL_FIPS
    if(1 != (r = (jint)FIPS_mode_set((int)mode))) {
      /* arrange to get a human-readable error message */
      unsigned long err = SSL_ERR_get();
      char msg[256];

      /* ERR_load_crypto_strings() already called in initialize() */

      ERR_error_string_n(err, msg, 256);

      tcn_ThrowException(e, msg);
    }
#elif (OPENSSL_VERSION_NUMBER > 0x2FFFFFFFL)
    /* This method should never be called when using OpenSSL 3.x onwards */
    tcn_ThrowException(e, "fipsModeSet is not supported in OpenSSL 3.x onwards.");
#else
    /* FIPS is unavailable */
    tcn_ThrowException(e, "FIPS was not available to tcnative at build time. You will need to re-build tcnative against an OpenSSL with FIPS.");
#endif

    return r;
}

/* OpenSSL Java Stream BIO */

typedef struct  {
    int            refcount;
    apr_pool_t     *pool;
    tcn_callback_t cb;
} BIO_JAVA;


static apr_status_t generic_bio_cleanup(void *data)
{
    BIO *b = (BIO *)data;

    if (b) {
        BIO_free(b);
    }
    return APR_SUCCESS;
}

void SSL_BIO_close(BIO *bi)
{
    BIO_JAVA *j;
    if (bi == NULL)
        return;
    j = (BIO_JAVA *)BIO_get_data(bi);
    if (j != NULL && BIO_test_flags(bi, SSL_BIO_FLAG_CALLBACK)) {
        j->refcount--;
        if (j->refcount == 0) {
            if (j->pool)
                apr_pool_cleanup_run(j->pool, bi, generic_bio_cleanup);
            else
                BIO_free(bi);
        }
    }
    else
        BIO_free(bi);
}

void SSL_BIO_doref(BIO *bi)
{
    BIO_JAVA *j;
    if (bi == NULL)
        return;
    j = (BIO_JAVA *)BIO_get_data(bi);
    if (j != NULL && BIO_test_flags(bi, SSL_BIO_FLAG_CALLBACK)) {
        j->refcount++;
    }
}


static int jbs_new(BIO *bi)
{
    BIO_JAVA *j;

    if ((j = OPENSSL_malloc(sizeof(BIO_JAVA))) == NULL)
        return 0;
    j->pool      = NULL;
    j->refcount  = 1;
    BIO_set_shutdown(bi, 1);
    BIO_set_init(bi, 0);
#if OPENSSL_VERSION_NUMBER < 0x10100000L
    /* No setter method for OpenSSL 1.1.0 available,
     * but I can't find any functional use of the
     * "num" field there either.
     */
    bi->num      = -1;
#endif
    BIO_set_data(bi, (void *)j);

    return 1;
}

static int jbs_free(BIO *bi)
{
    BIO_JAVA *j;
    if (bi == NULL)
        return 0;
    j = (BIO_JAVA *)BIO_get_data(bi);
    if (j != NULL) {
        if (BIO_get_init(bi)) {
            JNIEnv   *e = NULL;
            BIO_set_init(bi, 0);
            tcn_get_java_env(&e);
            TCN_UNLOAD_CLASS(e, j->cb.obj);
        }
        OPENSSL_free(j);
    }
    BIO_set_data(bi, NULL);
    return 1;
}

static int jbs_write(BIO *b, const char *in, int inl)
{
    jint ret = -1;
    if (BIO_get_init(b) && in != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)BIO_get_data(b);
        JNIEnv   *e = NULL;
        jbyteArray jb;
        tcn_get_java_env(&e);
        jb = (*e)->NewByteArray(e, inl);
        if (!(*e)->ExceptionOccurred(e)) {
            BIO_clear_retry_flags(b);
            (*e)->SetByteArrayRegion(e, jb, 0, inl, (jbyte *)in);
            ret = (*e)->CallIntMethod(e, j->cb.obj,
                                      j->cb.mid[0], jb);
            (*e)->ReleaseByteArrayElements(e, jb, (jbyte *)in, JNI_ABORT);
            (*e)->DeleteLocalRef(e, jb);
        }
    }
    /* From netty-tc-native, in the AF we were returning 0 */
    if (ret == 0) {
        BIO_set_retry_write(b);
        ret = -1;
    }
    return ret;
}

static int jbs_read(BIO *b, char *out, int outl)
{
    jint ret = 0;
    if (BIO_get_init(b) && out != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)BIO_get_data(b);
        JNIEnv   *e = NULL;
        jbyteArray jb;
        tcn_get_java_env(&e);
        jb = (*e)->NewByteArray(e, outl);
        if (!(*e)->ExceptionOccurred(e)) {
            BIO_clear_retry_flags(b);
            ret = (*e)->CallIntMethod(e, j->cb.obj,
                                      j->cb.mid[1], jb);
            if (ret > 0) {
                jbyte *jout = (*e)->GetPrimitiveArrayCritical(e, jb, NULL);
                memcpy(out, jout, ret);
                (*e)->ReleasePrimitiveArrayCritical(e, jb, jout, 0);
            } else if (outl != 0) {
                ret = -1;
                BIO_set_retry_read(b);
            }
            (*e)->DeleteLocalRef(e, jb);
        }
    }
    return ret;
}

static int jbs_puts(BIO *b, const char *in)
{
    int ret = 0;
    if (BIO_get_init(b) && in != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)BIO_get_data(b);
        JNIEnv   *e = NULL;
        tcn_get_java_env(&e);
        ret = (*e)->CallIntMethod(e, j->cb.obj,
                                  j->cb.mid[2],
                                  tcn_new_string(e, in));
    }
    return ret;
}

static int jbs_gets(BIO *b, char *out, int outl)
{
    int ret = 0;
    if (BIO_get_init(b) && out != NULL) {
        BIO_JAVA *j = (BIO_JAVA *)BIO_get_data(b);
        JNIEnv   *e = NULL;
        jobject  o;
        tcn_get_java_env(&e);
        if ((o = (*e)->CallObjectMethod(e, j->cb.obj,
                            j->cb.mid[3], (jint)(outl - 1)))) {
            TCN_ALLOC_CSTRING(o);
            if (J2S(o)) {
                int l = (int)strlen(J2S(o));
                if (l < outl) {
                    strcpy(out, J2S(o));
                    ret = outl;
                }
            }
            TCN_FREE_CSTRING(o);
        }
    }
    return ret;
}

static long jbs_ctrl(BIO *b, int cmd, long num, void *ptr)
{
    int ret = 0;
    switch (cmd) {
        case BIO_CTRL_FLUSH:
            ret = 1;
            break;
        default:
            ret = 0;
            break;
    }
    return ret;
}

#if OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)
static BIO_METHOD jbs_methods = {
    BIO_TYPE_FILE,
    "Java Callback",
    jbs_write,
    jbs_read,
    jbs_puts,
    jbs_gets,
    jbs_ctrl,
    jbs_new,
    jbs_free,
    NULL
};
#else
static BIO_METHOD *jbs_methods = NULL;

static void init_bio_methods(void)
{
    jbs_methods = BIO_meth_new(BIO_TYPE_FILE, "Java Callback");
    BIO_meth_set_write(jbs_methods, &jbs_write);
    BIO_meth_set_read(jbs_methods, &jbs_read);
    BIO_meth_set_puts(jbs_methods, &jbs_puts);
    BIO_meth_set_gets(jbs_methods, &jbs_gets);
    BIO_meth_set_ctrl(jbs_methods, &jbs_ctrl);
    BIO_meth_set_create(jbs_methods, &jbs_new);
    BIO_meth_set_destroy(jbs_methods, &jbs_free);
}

static void free_bio_methods(void)
{
    BIO_meth_free(jbs_methods);
}
#endif

static BIO_METHOD *BIO_jbs()
{
#if OPENSSL_VERSION_NUMBER < 0x10100000L || defined(LIBRESSL_VERSION_NUMBER)
    return(&jbs_methods);
#else
    return jbs_methods;
#endif
}

TCN_IMPLEMENT_CALL(jlong, SSL, newBIO)(TCN_STDARGS, jlong pool,
                                       jobject callback)
{
    BIO *bio = NULL;
    BIO_JAVA *j;
    jclass cls;

    UNREFERENCED(o);

    if ((bio = BIO_new(BIO_jbs())) == NULL) {
        tcn_ThrowException(e, "Create BIO failed");
        goto init_failed;
    }
    j = (BIO_JAVA *)BIO_get_data(bio);
    if (j == NULL) {
        tcn_ThrowException(e, "Create BIO failed");
        goto init_failed;
    }
    j->pool = J2P(pool, apr_pool_t *);
    if (j->pool) {
        apr_pool_cleanup_register(j->pool, (const void *)bio,
                                  generic_bio_cleanup,
                                  apr_pool_cleanup_null);
    }

    cls = (*e)->GetObjectClass(e, callback);
    j->cb.mid[0] = (*e)->GetMethodID(e, cls, "write", "([B)I");
    j->cb.mid[1] = (*e)->GetMethodID(e, cls, "read",  "([B)I");
    j->cb.mid[2] = (*e)->GetMethodID(e, cls, "puts",  "(Ljava/lang/String;)I");
    j->cb.mid[3] = (*e)->GetMethodID(e, cls, "gets",  "(I)Ljava/lang/String;");
    /* TODO: Check if method id's are valid */
    j->cb.obj    = (*e)->NewGlobalRef(e, callback);

    BIO_set_init(bio, 1);
    BIO_set_flags(bio, SSL_BIO_FLAG_CALLBACK);
    return P2J(bio);
init_failed:
    return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, closeBIO)(TCN_STDARGS, jlong bio)
{
    BIO *b = J2P(bio, BIO *);
    UNREFERENCED_STDARGS;
    SSL_BIO_close(b);
    return APR_SUCCESS;
}

TCN_IMPLEMENT_CALL(void, SSL, setPasswordCallback)(TCN_STDARGS,
                                                   jobject callback)
{
    jclass cls;

    UNREFERENCED(o);
    if (tcn_password_callback.cb.obj) {
        TCN_UNLOAD_CLASS(e,
                         tcn_password_callback.cb.obj);
    }
    cls = (*e)->GetObjectClass(e, callback);
    tcn_password_callback.cb.mid[0] = (*e)->GetMethodID(e, cls, "callback",
                           "(Ljava/lang/String;)Ljava/lang/String;");
    /* TODO: Check if method id is valid */
    tcn_password_callback.cb.obj    = (*e)->NewGlobalRef(e, callback);

}

TCN_IMPLEMENT_CALL(void, SSL, setPassword)(TCN_STDARGS, jstring password)
{
    TCN_ALLOC_CSTRING(password);
    UNREFERENCED(o);
    if (J2S(password)) {
        strncpy(tcn_password_callback.password, J2S(password), SSL_MAX_PASSWORD_LEN);
        tcn_password_callback.password[SSL_MAX_PASSWORD_LEN-1] = '\0';
    }
    TCN_FREE_CSTRING(password);
}

TCN_IMPLEMENT_CALL(jstring, SSL, getLastError)(TCN_STDARGS)
{
    char buf[256];
    UNREFERENCED(o);
    ERR_error_string(SSL_ERR_get(), buf);
    return tcn_new_string(e, buf);
}

TCN_IMPLEMENT_CALL(jboolean, SSL, hasOp)(TCN_STDARGS, jint op)
{
    return op == (op & supported_ssl_opts) ? JNI_TRUE : JNI_FALSE;
}

/*** Begin Twitter 1:1 API addition ***/
TCN_IMPLEMENT_CALL(jint, SSL, getLastErrorNumber)(TCN_STDARGS) {
    UNREFERENCED_STDARGS;
    return SSL_ERR_get();
}

static void ssl_info_callback(const SSL *ssl, int where, int ret) {
    int *handshakeCount = NULL;
    if (0 != (where & SSL_CB_HANDSHAKE_DONE)) {
        handshakeCount = (int*) SSL_get_app_data3(ssl);
        if (handshakeCount != NULL) {
            ++(*handshakeCount);
        }
    }
}

static apr_status_t ssl_con_pool_cleanup(void *data)
{
    SSL *ssl = (SSL*) data;
    int *destroyCount;

    TCN_ASSERT(ssl != 0);

    destroyCount = SSL_get_app_data4(ssl);
    if (destroyCount != NULL) {
        ++(*destroyCount);
    }

    return APR_SUCCESS;
}

TCN_IMPLEMENT_CALL(jlong /* SSL * */, SSL, newSSL)(TCN_STDARGS,
                                                   jlong ctx /* tcn_ssl_ctxt_t * */,
                                                   jboolean server) {
    tcn_ssl_ctxt_t *c = J2P(ctx, tcn_ssl_ctxt_t *);
    int *handshakeCount = malloc(sizeof(int));
    int *destroyCount = malloc(sizeof(int));
    SSL *ssl;
    apr_pool_t *p = NULL;
    tcn_ssl_conn_t *con;

    UNREFERENCED_STDARGS;

    TCN_ASSERT(ctx != 0);

    ssl = SSL_new(c->ctx);
    if (ssl == NULL) {
        free(handshakeCount);
        free(destroyCount);
        tcn_ThrowException(e, "cannot create new ssl");
        return 0;
    }

    apr_pool_create(&p, c->pool);
    if (p == NULL) {
        free(handshakeCount);
        free(destroyCount);
        SSL_free(ssl);
        tcn_ThrowAPRException(e, apr_get_os_error());
        return 0;
    }

    if ((con = apr_pcalloc(p, sizeof(tcn_ssl_conn_t))) == NULL) {
        free(handshakeCount);
        free(destroyCount);
        SSL_free(ssl);
        apr_pool_destroy(p);
        tcn_ThrowAPRException(e, apr_get_os_error());
        return 0;
    }
    con->pool = p;
    con->ctx  = c;
    con->ssl  = ssl;
    con->shutdown_type = c->shutdown_type;

    /* Store the handshakeCount in the SSL instance. */
    *handshakeCount = 0;
    SSL_set_app_data3(ssl, handshakeCount);

    /* Store the destroyCount in the SSL instance. */
    *destroyCount = 0;
    SSL_set_app_data4(ssl, destroyCount);

    /* Add callback to keep track of handshakes. */
    SSL_CTX_set_info_callback(c->ctx, ssl_info_callback);

    if (server) {
        SSL_set_accept_state(ssl);
    } else {
        SSL_set_connect_state(ssl);
    }

    /* Setup verify and seed */
    SSL_set_verify_result(ssl, X509_V_OK);
    SSL_rand_seed(c->rand_file);

    /* Store for later usage in SSL_callback_SSL_verify */
    SSL_set_app_data2(ssl, c);
    SSL_set_app_data(ssl, con);
    /* Register cleanup that prevent double destruction */
    apr_pool_cleanup_register(con->pool, (const void *)ssl,
                              ssl_con_pool_cleanup,
                              apr_pool_cleanup_null);

    return P2J(ssl);
}

TCN_IMPLEMENT_CALL(void, SSL, setBIO)(TCN_STDARGS,
                                      jlong ssl /* SSL * */,
                                      jlong rbio /* BIO * */,
                                      jlong wbio /* BIO * */) {
    UNREFERENCED_STDARGS;
    SSL_set_bio(J2P(ssl, SSL *), J2P(rbio, BIO *), J2P(wbio, BIO *));
    return;
}

TCN_IMPLEMENT_CALL(jint, SSL, getError)(TCN_STDARGS,
                                       jlong ssl /* SSL * */,
                                       jint ret) {
    UNREFERENCED_STDARGS;
    return SSL_get_error(J2P(ssl, SSL*), ret);
}

/* How much did SSL write into this BIO? */
TCN_IMPLEMENT_CALL(jint /* nbytes */, SSL, pendingWrittenBytesInBIO)(TCN_STDARGS,
                                                                     jlong bio /* BIO * */) {
    UNREFERENCED_STDARGS;

    return BIO_ctrl_pending(J2P(bio, BIO *));
}

/* How much is available for reading in the given SSL struct? */
TCN_IMPLEMENT_CALL(jint, SSL, pendingReadableBytesInSSL)(TCN_STDARGS, jlong ssl /* SSL * */) {
    UNREFERENCED_STDARGS;

    return SSL_pending(J2P(ssl, SSL *));
}

/* Write wlen bytes from wbuf into bio */
TCN_IMPLEMENT_CALL(jint /* status */, SSL, writeToBIO)(TCN_STDARGS,
                                                       jlong bio /* BIO * */,
                                                       jlong wbuf /* char* */,
                                                       jint wlen /* sizeof(wbuf) */) {
    UNREFERENCED_STDARGS;

    return BIO_write(J2P(bio, BIO *), J2P(wbuf, void *), wlen);

}

/* Read up to rlen bytes from bio into rbuf */
TCN_IMPLEMENT_CALL(jint /* status */, SSL, readFromBIO)(TCN_STDARGS,
                                                        jlong bio /* BIO * */,
                                                        jlong rbuf /* char * */,
                                                        jint rlen /* sizeof(rbuf) - 1 */) {
    UNREFERENCED_STDARGS;

    return BIO_read(J2P(bio, BIO *), J2P(rbuf, void *), rlen);
}

/* Write up to wlen bytes of application data to the ssl BIO (encrypt) */
TCN_IMPLEMENT_CALL(jint /* status */, SSL, writeToSSL)(TCN_STDARGS,
                                                       jlong ssl /* SSL * */,
                                                       jlong wbuf /* char * */,
                                                       jint wlen /* sizeof(wbuf) */) {
    UNREFERENCED_STDARGS;

    return SSL_write(J2P(ssl, SSL *), J2P(wbuf, void *), wlen);
}

/* Read up to rlen bytes of application data from the given SSL BIO (decrypt) */
TCN_IMPLEMENT_CALL(jint /* status */, SSL, readFromSSL)(TCN_STDARGS,
                                                        jlong ssl /* SSL * */,
                                                        jlong rbuf /* char * */,
                                                        jint rlen /* sizeof(rbuf) - 1 */) {
    UNREFERENCED_STDARGS;

    return SSL_read(J2P(ssl, SSL *), J2P(rbuf, void *), rlen);
}

/* Get the shutdown status of the engine */
TCN_IMPLEMENT_CALL(jint /* status */, SSL, getShutdown)(TCN_STDARGS,
                                                        jlong ssl /* SSL * */) {
    UNREFERENCED_STDARGS;

    return SSL_get_shutdown(J2P(ssl, SSL *));
}

/* Called when the peer closes the connection */
TCN_IMPLEMENT_CALL(void, SSL, setShutdown)(TCN_STDARGS,
                                           jlong ssl /* SSL * */,
                                           jint mode) {
    UNREFERENCED_STDARGS;

    SSL_set_shutdown(J2P(ssl, SSL *), mode);
}

/* Free the SSL * and its associated internal BIO */
TCN_IMPLEMENT_CALL(void, SSL, freeSSL)(TCN_STDARGS,
                                       jlong ssl /* SSL * */) {
    SSL *ssl_ = J2P(ssl, SSL *);
    int *handshakeCount = SSL_get_app_data3(ssl_);
    int *destroyCount = SSL_get_app_data4(ssl_);
    tcn_ssl_conn_t *con = SSL_get_app_data(ssl_);

    UNREFERENCED_STDARGS;

    if (destroyCount != NULL) {
        if (*destroyCount == 0) {
            apr_pool_destroy(con->pool);
        }
        free(destroyCount);
    }
    if (handshakeCount != NULL) {
        free(handshakeCount);
    }
    SSL_free(ssl_);
}

/* Make a BIO pair (network and internal) for the provided SSL * and return the network BIO */
TCN_IMPLEMENT_CALL(jlong, SSL, makeNetworkBIO)(TCN_STDARGS,
                                               jlong ssl /* SSL * */) {
    SSL *ssl_ = J2P(ssl, SSL *);
    BIO *internal_bio;
    BIO *network_bio;

    UNREFERENCED(o);

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        goto fail;
    }

    if (BIO_new_bio_pair(&internal_bio, 0, &network_bio, 0) != 1) {
        tcn_ThrowException(e, "BIO_new_bio_pair failed");
        goto fail;
    }

    SSL_set_bio(ssl_, internal_bio, internal_bio);

    return P2J(network_bio);
 fail:
    return 0;
}

/* Free a BIO * (typically, the network BIO) */
TCN_IMPLEMENT_CALL(void, SSL, freeBIO)(TCN_STDARGS,
                                       jlong bio /* BIO * */) {
    BIO *bio_;
    UNREFERENCED_STDARGS;

    bio_ = J2P(bio, BIO *);
    BIO_free(bio_);
}

/* Send CLOSE_NOTIFY to peer */
TCN_IMPLEMENT_CALL(jint /* status */, SSL, shutdownSSL)(TCN_STDARGS,
                                                        jlong ssl /* SSL * */) {
    UNREFERENCED_STDARGS;

    return SSL_shutdown(J2P(ssl, SSL *));
}

/* Read which cipher was negotiated for the given SSL *. */
TCN_IMPLEMENT_CALL(jstring, SSL, getCipherForSSL)(TCN_STDARGS,
                                                  jlong ssl /* SSL * */)
{
    UNREFERENCED_STDARGS;

    return AJP_TO_JSTRING(SSL_get_cipher(J2P(ssl, SSL*)));
}

/* Read which protocol was negotiated for the given SSL *. */
TCN_IMPLEMENT_CALL(jstring, SSL, getVersion)(TCN_STDARGS,
                                                  jlong ssl /* SSL * */)
{
    UNREFERENCED_STDARGS;

    return AJP_TO_JSTRING(SSL_get_version(J2P(ssl, SSL*)));
}

/* Is the handshake over yet? */
TCN_IMPLEMENT_CALL(jint, SSL, isInInit)(TCN_STDARGS,
                                        jlong ssl /* SSL * */) {
    SSL *ssl_ = J2P(ssl, SSL *);

    UNREFERENCED(o);

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    } else {
        return SSL_in_init(ssl_);
    }
}

TCN_IMPLEMENT_CALL(jint, SSL, doHandshake)(TCN_STDARGS,
                                           jlong ssl /* SSL * */) {
    SSL *ssl_ = J2P(ssl, SSL *);
    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    }

    UNREFERENCED(o);

    return SSL_do_handshake(ssl_);
}

TCN_IMPLEMENT_CALL(jint, SSL, renegotiate)(TCN_STDARGS,
                                           jlong ssl /* SSL * */) {
    SSL *ssl_ = J2P(ssl, SSL *);
    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    }

    UNREFERENCED(o);

    return SSL_renegotiate(ssl_);
}

TCN_IMPLEMENT_CALL(jint, SSL, renegotiatePending)(TCN_STDARGS,
                                                  jlong ssl /* SSL * */) {
    SSL *ssl_ = J2P(ssl, SSL *);
    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    }

    UNREFERENCED(o);

    return SSL_renegotiate_pending(ssl_);
}

TCN_IMPLEMENT_CALL(jint, SSL, verifyClientPostHandshake)(TCN_STDARGS,
                                                         jlong ssl /* SSL * */) {
#if defined(SSL_OP_NO_TLSv1_3)
    SSL *ssl_ = J2P(ssl, SSL *);
    tcn_ssl_conn_t *con;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    }

    UNREFERENCED(o);

    con = (tcn_ssl_conn_t *)SSL_get_app_data(ssl_);
    con->pha_state = PHA_STARTED;

    return SSL_verify_client_post_handshake(ssl_);
#else
    return 0;
#endif
}

TCN_IMPLEMENT_CALL(jint, SSL, getPostHandshakeAuthInProgress)(TCN_STDARGS,
                                                              jlong ssl /* SSL * */) {
#if defined(SSL_OP_NO_TLSv1_3)
    SSL *ssl_ = J2P(ssl, SSL *);
    tcn_ssl_conn_t *con;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    }

    UNREFERENCED(o);

    con = (tcn_ssl_conn_t *)SSL_get_app_data(ssl_);

    return (con->pha_state == PHA_STARTED);
#else
    return 0;
#endif
}

/* Read which protocol was negotiated for the given SSL *. */
TCN_IMPLEMENT_CALL(jstring, SSL, getNextProtoNegotiated)(TCN_STDARGS,
                                                         jlong ssl /* SSL * */) {
    SSL *ssl_ = J2P(ssl, SSL *);
    const unsigned char *proto;
    unsigned int proto_len;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return NULL;
    }

    UNREFERENCED(o);

    SSL_get0_next_proto_negotiated(ssl_, &proto, &proto_len);
    return tcn_new_stringn(e, (const char *)proto, (size_t) proto_len);
}

/*** End Twitter API Additions ***/

/*** Apple API Additions ***/

TCN_IMPLEMENT_CALL(jstring, SSL, getAlpnSelected)(TCN_STDARGS,
                                                         jlong ssl /* SSL * */) {
    /* Looks fishy we have the same in sslnetwork.c, it set by socket/connection */
    SSL *ssl_ = J2P(ssl, SSL *);
    const unsigned char *proto;
    unsigned int proto_len;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return NULL;
    }

    UNREFERENCED(o);

    SSL_get0_alpn_selected(ssl_, &proto, &proto_len);
    return tcn_new_stringn(e, (const char *) proto, (size_t) proto_len);
}

TCN_IMPLEMENT_CALL(jobjectArray, SSL, getPeerCertChain)(TCN_STDARGS,
                                                  jlong ssl /* SSL * */)
{
    STACK_OF(X509) *sk;
    int len;
    int i;
    X509 *cert;
    int length;
    unsigned char *buf;
    jobjectArray array;
    jbyteArray bArray;

    SSL *ssl_ = J2P(ssl, SSL *);

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return NULL;
    }

    UNREFERENCED(o);

    // Get a stack of all certs in the chain.
    sk = SSL_get_peer_cert_chain(ssl_);

    len = sk_X509_num(sk);
    if (len <= 0) {
        /* No peer certificate chain as no auth took place yet, or the auth was not successful. */
        return NULL;
    }
    /* Create the byte[][] array that holds all the certs */
    array = (*e)->NewObjectArray(e, len, byteArrayClass, NULL);

    for(i = 0; i < len; i++) {
        cert = (X509*) sk_X509_value(sk, i);

        buf = NULL;
        length = i2d_X509(cert, &buf);
        if (length < 0) {
            OPENSSL_free(buf);
            /* In case of error just return an empty byte[][] */
            return (*e)->NewObjectArray(e, 0, byteArrayClass, NULL);
        }
        bArray = (*e)->NewByteArray(e, length);
        (*e)->SetByteArrayRegion(e, bArray, 0, length, (jbyte*) buf);
        (*e)->SetObjectArrayElement(e, array, i, bArray);

        /*
         * Delete the local reference as we not know how long the chain is and local references are otherwise
         * only freed once jni method returns.
         */
        (*e)->DeleteLocalRef(e, bArray);

        OPENSSL_free(buf);
    }
    return array;
}

TCN_IMPLEMENT_CALL(jbyteArray, SSL, getPeerCertificate)(TCN_STDARGS,
                                                  jlong ssl /* SSL * */)
{
    X509 *cert;
    int length;
    unsigned char *buf = NULL;
    jbyteArray bArray;

    SSL *ssl_ = J2P(ssl, SSL *);

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return NULL;
    }

    UNREFERENCED(o);

    /* Get a stack of all certs in the chain */
    cert = SSL_get_peer_certificate(ssl_);
    if (cert == NULL) {
        return NULL;
    }

    length = i2d_X509(cert, &buf);

    bArray = (*e)->NewByteArray(e, length);
    (*e)->SetByteArrayRegion(e, bArray, 0, length, (jbyte*) buf);

    /*
     * We need to free the cert as the reference count is incremented by one and it is not destroyed when the
     * session is freed.
     * See https://www.openssl.org/docs/ssl/SSL_get_peer_certificate.html
     */
    X509_free(cert);

    OPENSSL_free(buf);

    return bArray;
}

TCN_IMPLEMENT_CALL(jstring, SSL, getErrorString)(TCN_STDARGS, jlong number)
{
    char buf[256];
    UNREFERENCED(o);
    ERR_error_string(number, buf);
    return tcn_new_string(e, buf);
}

TCN_IMPLEMENT_CALL(jlong, SSL, getTime)(TCN_STDARGS, jlong ssl)
{
    const SSL *ssl_ = J2P(ssl, SSL *);
    const SSL_SESSION *session;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    }

    UNREFERENCED(o);

    session  = SSL_get_session(ssl_);
    if (session) {
        return SSL_get_time(session);
    } else {
        tcn_ThrowException(e, "ssl session is null");
        return 0;
    }
}

TCN_IMPLEMENT_CALL(void, SSL, setVerify)(TCN_STDARGS, jlong ssl,
                                                jint level, jint depth)
{
    tcn_ssl_ctxt_t *c;
    int verify;
    SSL *ssl_ = J2P(ssl, SSL *);

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return;
    }

    c = SSL_get_app_data2(ssl_);

    verify = SSL_VERIFY_NONE;

    UNREFERENCED(o);

    if (c == NULL) {
        tcn_ThrowException(e, "context is null");
        return;
    }
    c->verify_mode = level;

    if (c->verify_mode == SSL_CVERIFY_UNSET)
        c->verify_mode = SSL_CVERIFY_NONE;
    if (depth > 0)
        c->verify_depth = depth;
    /*
     *  Configure callbacks for SSL context
     */
    if (c->verify_mode == SSL_CVERIFY_REQUIRE)
        verify |= SSL_VERIFY_PEER_STRICT;
    if ((c->verify_mode == SSL_CVERIFY_OPTIONAL) ||
        (c->verify_mode == SSL_CVERIFY_OPTIONAL_NO_CA))
        verify |= SSL_VERIFY_PEER;
    if (!c->store) {
        if (SSL_CTX_set_default_verify_paths(c->ctx)) {
            c->store = SSL_CTX_get_cert_store(c->ctx);
            X509_STORE_set_flags(c->store, 0);
        }
        else {
            /* XXX: See if this is fatal */
        }
    }

    SSL_set_verify(ssl_, verify, SSL_callback_SSL_verify);
}

TCN_IMPLEMENT_CALL(void, SSL, setOptions)(TCN_STDARGS, jlong ssl,
                                                 jint opt)
{
    SSL *ssl_ = J2P(ssl, SSL *);

    UNREFERENCED_STDARGS;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return;
    }

#ifndef SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION
    /* Clear the flag if not supported */
    if (opt & 0x00040000) {
        opt &= ~0x00040000;
    }
#endif
    SSL_set_options(ssl_, opt);
}

TCN_IMPLEMENT_CALL(jint, SSL, getOptions)(TCN_STDARGS, jlong ssl)
{
    SSL *ssl_ = J2P(ssl, SSL *);

    UNREFERENCED_STDARGS;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return 0;
    }

    return SSL_get_options(ssl_);
}

TCN_IMPLEMENT_CALL(jobjectArray, SSL, getCiphers)(TCN_STDARGS, jlong ssl)
{
    STACK_OF(SSL_CIPHER) *sk;
    int len;
    jobjectArray array;
    SSL_CIPHER *cipher;
    const char *name;
    int i;
    jstring c_name;
    SSL *ssl_ = J2P(ssl, SSL *);

    UNREFERENCED_STDARGS;

    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return NULL;
    }

    sk = SSL_get_ciphers(ssl_);
    len = sk_SSL_CIPHER_num(sk);

    if (len <= 0) {
        /* No peer certificate chain as no auth took place yet, or the auth was not successful. */
        return NULL;
    }

    /* Create the byte[][] array that holds all the certs */
    array = (*e)->NewObjectArray(e, len, stringClass, NULL);

    for (i = 0; i < len; i++) {
        cipher = (SSL_CIPHER*) sk_SSL_CIPHER_value(sk, i);
        name = SSL_CIPHER_get_name(cipher);

        c_name = (*e)->NewStringUTF(e, name);
        (*e)->SetObjectArrayElement(e, array, i, c_name);
    }
    return array;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, setCipherSuites)(TCN_STDARGS, jlong ssl,
                                                         jstring ciphers)
{
    jboolean rv = JNI_TRUE;
    SSL *ssl_ = J2P(ssl, SSL *);
    TCN_ALLOC_CSTRING(ciphers);

    UNREFERENCED_STDARGS;

    if (ssl_ == NULL) {
        TCN_FREE_CSTRING(ciphers);
        tcn_ThrowException(e, "ssl is null");
        return JNI_FALSE;
    }

    UNREFERENCED(o);
    if (!J2S(ciphers)) {
        TCN_FREE_CSTRING(ciphers);
        return JNI_FALSE;
    }
    if (!SSL_set_cipher_list(ssl_, J2S(ciphers))) {
        char err[256];
        ERR_error_string(SSL_ERR_get(), err);
        tcn_Throw(e, "Unable to configure permitted SSL ciphers (%s)", err);
        rv = JNI_FALSE;
    }
    TCN_FREE_CSTRING(ciphers);
    return rv;
}

TCN_IMPLEMENT_CALL(jbyteArray, SSL, getSessionId)(TCN_STDARGS, jlong ssl)
{

    unsigned int len;
    const unsigned char *session_id;
    const SSL_SESSION *session;
    jbyteArray bArray;
    SSL *ssl_ = J2P(ssl, SSL *);
    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return NULL;
    }
    UNREFERENCED(o);
    session = SSL_get_session(ssl_);
    if (NULL == session) {
        return NULL;
    }

    session_id = SSL_SESSION_get_id(session, &len);

    if (len == 0 || session_id == NULL) {
        return NULL;
    }

    bArray = (*e)->NewByteArray(e, len);
    (*e)->SetByteArrayRegion(e, bArray, 0, len, (jbyte*) session_id);
    return bArray;
}

TCN_IMPLEMENT_CALL(jint, SSL, getHandshakeCount)(TCN_STDARGS, jlong ssl)
{
    int *handshakeCount = NULL;
    SSL *ssl_ = J2P(ssl, SSL *);
    if (ssl_ == NULL) {
        tcn_ThrowException(e, "ssl is null");
        return -1;
    }
    UNREFERENCED(o);

    handshakeCount = SSL_get_app_data3(ssl_);
    if (handshakeCount != NULL) {
        return *handshakeCount;
    }
    return 0;
}

/*** End Apple API Additions ***/

#else /* HAVE_OPENSSL */
/* OpenSSL is not supported.
 * Create empty stubs.
 */

TCN_IMPLEMENT_CALL(jint, SSL, version)(TCN_STDARGS)
{
    UNREFERENCED_STDARGS;
    return 0;
}

TCN_IMPLEMENT_CALL(jstring, SSL, versionString)(TCN_STDARGS)
{
    UNREFERENCED_STDARGS;
    return NULL;
}

TCN_IMPLEMENT_CALL(jint, SSL, initialize)(TCN_STDARGS, jstring engine)
{
    UNREFERENCED(o);
    UNREFERENCED(engine);
    tcn_ThrowAPRException(e, APR_ENOTIMPL);
    return (jint)APR_ENOTIMPL;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randLoad)(TCN_STDARGS, jstring file)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(file);
    return JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randSave)(TCN_STDARGS, jstring file)
{
    UNREFERENCED_STDARGS;
    return JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, randMake)(TCN_STDARGS, jstring file,
                                            jint length, jboolean base64)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(file);
    UNREFERENCED(length);
    UNREFERENCED(base64);
    return JNI_FALSE;
}

TCN_IMPLEMENT_CALL(void, SSL, randSet)(TCN_STDARGS, jstring file)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(file);
}

TCN_IMPLEMENT_CALL(jint, SSL, fipsModeGet)(TCN_STDARGS)
{
    UNREFERENCED(o);
    tcn_ThrowException(e, "FIPS was not available to tcnative at build time. You will need to re-build tcnative against an OpenSSL with FIPS.");
    return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, fipsModeSet)(TCN_STDARGS, jint mode)
{
    UNREFERENCED(o);
    UNREFERENCED(mode);
    tcn_ThrowException(e, "FIPS was not available to tcnative at build time. You will need to re-build tcnative against an OpenSSL with FIPS.");
    return 0;
}

TCN_IMPLEMENT_CALL(jlong, SSL, newBIO)(TCN_STDARGS, jlong pool,
                                       jobject callback)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(pool);
    UNREFERENCED(callback);
    return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, closeBIO)(TCN_STDARGS, jlong bio)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(bio);
    return (jint)APR_ENOTIMPL;
}

TCN_IMPLEMENT_CALL(void, SSL, setPasswordCallback)(TCN_STDARGS,
                                                   jobject callback)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(callback);
}

TCN_IMPLEMENT_CALL(void, SSL, setPassword)(TCN_STDARGS, jstring password)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(password);
}

TCN_IMPLEMENT_CALL(jboolean, SSL, generateRSATempKey)(TCN_STDARGS, jint idx)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(idx);
    return JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, loadDSATempKey)(TCN_STDARGS, jint idx,
                                                  jstring file)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(idx);
    UNREFERENCED(file);
    return JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jstring, SSL, getLastError)(TCN_STDARGS)
{
    UNREFERENCED_STDARGS;
    return NULL;
}

TCN_IMPLEMENT_CALL(jboolean, SSL, hasOp)(TCN_STDARGS, jint op)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(op);
    return JNI_FALSE;
}

/*** Begin Twitter 1:1 API addition ***/
TCN_IMPLEMENT_CALL(jint, SSL, getLastErrorNumber)(TCN_STDARGS) {
  UNREFERENCED(o);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jlong /* SSL * */, SSL, newSSL)(TCN_STDARGS,
                                                   jlong ctx /* tcn_ssl_ctxt_t * */,
                                                   jboolean server) {
  UNREFERENCED(o);
  UNREFERENCED(ctx);
  UNREFERENCED(server);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(void, SSL, setBIO)(TCN_STDARGS, jlong ssl, jlong rbio, jlong wbio) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  UNREFERENCED(rbio);
  UNREFERENCED(wbio);
  tcn_ThrowException(e, "Not implemented");
}

TCN_IMPLEMENT_CALL(jint, SSL, pendingWrittenBytesInBIO)(TCN_STDARGS, jlong bio) {
  UNREFERENCED(o);
  UNREFERENCED(bio);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, pendingReadableBytesInSSL)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, writeToBIO)(TCN_STDARGS, jlong bio, jlong wbuf, jint wlen) {
  UNREFERENCED(o);
  UNREFERENCED(bio);
  UNREFERENCED(wbuf);
  UNREFERENCED(wlen);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, readFromBIO)(TCN_STDARGS, jlong bio, jlong rbuf, jint rlen) {
  UNREFERENCED(o);
  UNREFERENCED(bio);
  UNREFERENCED(rbuf);
  UNREFERENCED(rlen);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, writeToSSL)(TCN_STDARGS, jlong ssl, jlong wbuf, jint wlen) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  UNREFERENCED(wbuf);
  UNREFERENCED(wlen);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, readFromSSL)(TCN_STDARGS, jlong ssl, jlong rbuf, jint rlen) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  UNREFERENCED(rbuf);
  UNREFERENCED(rlen);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, getShutdown)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(void, SSL, setShutdown)(TCN_STDARGS, jlong ssl, jint mode) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  UNREFERENCED(mode);
  tcn_ThrowException(e, "Not implemented");
}

TCN_IMPLEMENT_CALL(void, SSL, freeSSL)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
}

TCN_IMPLEMENT_CALL(jlong, SSL, makeNetworkBIO)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(void, SSL, freeBIO)(TCN_STDARGS, jlong bio) {
  UNREFERENCED(o);
  UNREFERENCED(bio);
  tcn_ThrowException(e, "Not implemented");
}

TCN_IMPLEMENT_CALL(jint, SSL, shutdownSSL)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jstring, SSL, getCipherForSSL)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return NULL;
}

TCN_IMPLEMENT_CALL(jint, SSL, isInInit)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, doHandshake)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, renegotiate)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, renegotiatePending)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, verifyClientPostHandshake)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jint, SSL, getPostHandshakeAuthInProgress)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(jstring, SSL, getNextProtoNegotiated)(TCN_STDARGS, jlong ssl) {
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return NULL;
}

/*** End Twitter 1:1 API addition ***/

/*** Begin Apple 1:1 API addition ***/

TCN_IMPLEMENT_CALL(jstring, SSL, getAlpnSelected)(TCN_STDARGS, jlong ssl) {
    UNREFERENCED(o);
    UNREFERENCED(ssl);
    tcn_ThrowException(e, "Not implemented");
    return NULL;
}

TCN_IMPLEMENT_CALL(jobjectArray, SSL, getPeerCertChain)(TCN_STDARGS, jlong ssl)
{
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return NULL;
}

TCN_IMPLEMENT_CALL(jbyteArray, SSL, getPeerCertificate)(TCN_STDARGS, jlong ssl)
{
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return NULL;
}

TCN_IMPLEMENT_CALL(jstring, SSL, getErrorString)(TCN_STDARGS, jlong number)
{
  UNREFERENCED(o);
  UNREFERENCED(number);
  tcn_ThrowException(e, "Not implemented");
  return NULL;
}

TCN_IMPLEMENT_CALL(jstring, SSL, getVersion)(TCN_STDARGS, jlong ssl)
{
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return NULL;
}

TCN_IMPLEMENT_CALL(jlong, SSL, getTime)(TCN_STDARGS, jlong ssl)
{
  UNREFERENCED(o);
  UNREFERENCED(ssl);
  tcn_ThrowException(e, "Not implemented");
  return 0;
}

TCN_IMPLEMENT_CALL(void, SSL, setVerify)(TCN_STDARGS, jlong ssl,
                                                jint level, jint depth)
{
    UNREFERENCED(o);
    UNREFERENCED(ssl);
    tcn_ThrowException(e, "Not implemented");
}

TCN_IMPLEMENT_CALL(void, SSL, setOptions)(TCN_STDARGS, jlong ssl,
                                                 jint opt)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(ssl);
    UNREFERENCED(opt);
    tcn_ThrowException(e, "Not implemented");
}

TCN_IMPLEMENT_CALL(jint, SSL, getOptions)(TCN_STDARGS, jlong ssl)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(ssl);
    tcn_ThrowException(e, "Not implemented");
    return 0;
}
TCN_IMPLEMENT_CALL(jobjectArray, SSL, getCiphers)(TCN_STDARGS, jlong ssl)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(ssl);
    tcn_ThrowException(e, "Not implemented");
    return 0;
}
TCN_IMPLEMENT_CALL(jboolean, SSL, setCipherSuites)(TCN_STDARGS, jlong ssl,
                                                         jstring ciphers)
{
    UNREFERENCED_STDARGS;
    UNREFERENCED(ssl);
    UNREFERENCED(ciphers);
    tcn_ThrowException(e, "Not implemented");
    return JNI_FALSE;
}
TCN_IMPLEMENT_CALL(jbyteArray, SSL, getSessionId)(TCN_STDARGS, jlong ssl)
{
    UNREFERENCED(o);
    UNREFERENCED(ssl);
    tcn_ThrowException(e, "Not implemented");
    return 0;
}
TCN_IMPLEMENT_CALL(jint, SSL, getHandshakeCount)(TCN_STDARGS, jlong ssl)
{
    UNREFERENCED(o);
    UNREFERENCED(ssl);
    tcn_ThrowException(e, "Not implemented");
    return 0;
}
/*** End Apple API Additions ***/
#endif /* HAVE_OPENSSL */
