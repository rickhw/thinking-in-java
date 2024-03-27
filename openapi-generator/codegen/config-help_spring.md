
CONFIG OPTIONS

	additionalEnumTypeAnnotations
	    Additional annotations for enum type(class level annotations)

	additionalModelTypeAnnotations
	    Additional annotations for model type(class level annotations). List separated by semicolon(;) or new line (Linux or Windows)

	additionalOneOfTypeAnnotations
	    Additional annotations for oneOf interfaces(class level annotations). List separated by semicolon(;) or new line (Linux or Windows)

	allowUnicodeIdentifiers
	    boolean, toggles whether unicode identifiers are allowed in names or not, default is false (Default: false)

	annotationLibrary
	    Select the complementary documentation annotation library. (Default: swagger2)
	        none - Do not annotate Model and Api with complementary annotations.
	        swagger1 - Annotate Model and Api using the Swagger Annotations 1.x library.
	        swagger2 - Annotate Model and Api using the Swagger Annotations 2.x library.

	apiFirst
	    Generate the API from the OAI spec at server compile time (API first approach) (Default: false)

	apiPackage
	    package for generated api classes (Default: org.openapitools.api)

	artifactDescription
	    artifact description in generated pom.xml (Default: OpenAPI Java)

	artifactId
	    artifactId in generated pom.xml. This also becomes part of the generated library's filename (Default: openapi-spring)

	artifactUrl
	    artifact URL in generated pom.xml (Default: https://github.com/openapitools/openapi-generator)

	artifactVersion
	    artifact version in generated pom.xml. This also becomes part of the generated library's filename. If not provided, uses the version from the OpenAPI specification file. If that's also not present, uses the default value of the artifactVersion option. (Default: 1.0.0)

	async
	    use async Callable controllers (Default: false)

	basePackage
	    base package (invokerPackage) for generated code (Default: org.openapitools)

	bigDecimalAsString
	    Treat BigDecimal values as Strings to avoid precision loss. (Default: false)

	booleanGetterPrefix
	    Set booleanGetterPrefix (Default: get)

	camelCaseDollarSign
	    Fix camelCase when starting with $ sign. when true : $Value when false : $value (Default: false)

	configPackage
	    configuration package for generated code (Default: org.openapitools.configuration)

	containerDefaultToNull
	    Set containers (array, set, map) default to null (Default: false)

	dateLibrary
	    Option. Date library to use (Default: java8)
	        joda - Joda (for legacy app only)
	        legacy - Legacy java.util.Date
	        java8-localdatetime - Java 8 using LocalDateTime (for legacy app only)
	        java8 - Java 8 native JSR310 (preferred for jdk 1.8+)

	delegatePattern
	    Whether to generate the server files using the delegate pattern (Default: false)

	developerEmail
	    developer email in generated pom.xml (Default: team@openapitools.org)

	developerName
	    developer name in generated pom.xml (Default: OpenAPI-Generator Contributors)

	developerOrganization
	    developer organization in generated pom.xml (Default: OpenAPITools.org)

	developerOrganizationUrl
	    developer organization URL in generated pom.xml (Default: http://openapitools.org)

	disableHtmlEscaping
	    Disable HTML escaping of JSON strings when using gson (needed to avoid problems with byte[] fields) (Default: false)

	disallowAdditionalPropertiesIfNotPresent
	    If false, the 'additionalProperties' implementation (set to true by default) is compliant with the OAS and JSON schema specifications. If true (default), keep the old (incorrect) behaviour that 'additionalProperties' is set to false by default. (Default: true)
	        false - The 'additionalProperties' implementation is compliant with the OAS and JSON schema specifications.
	        true - Keep the old (incorrect) behaviour that 'additionalProperties' is set to false by default.

	discriminatorCaseSensitive
	    Whether the discriminator value lookup should be case-sensitive or not. This option only works for Java API client (Default: true)

	documentationProvider
	    Select the OpenAPI documentation provider. (Default: springdoc)
	        none - Do not publish an OpenAPI specification.
	        source - Publish the original input OpenAPI specification.
	        springfox - Generate an OpenAPI 2 (fka Swagger RESTful API Documentation Specification) specification using SpringFox 2.x. Deprecated (for removal); use springdoc instead.
	        springdoc - Generate an OpenAPI 3 specification using SpringDoc.

	ensureUniqueParams
	    Whether to ensure parameter names are unique in an operation (rename parameters that are not). (Default: true)

	enumUnknownDefaultCase
	    If the server adds new enum cases, that are unknown by an old spec/client, the client will fail to parse the network response.With this option enabled, each enum will have a new case, 'unknown_default_open_api', so that when the server sends an enum case that is not known by the client/spec, they can safely fallback to this case. (Default: false)
	        false - No changes to the enum's are made, this is the default option.
	        true - With this option enabled, each enum will have a new case, 'unknown_default_open_api', so that when the enum case sent by the server is not known by the client/spec, can safely be decoded to this case.

	generatedConstructorWithRequiredArgs
	    Whether to generate constructors with required args for models (Default: true)

	groupId
	    groupId in generated pom.xml (Default: org.openapitools)

	hateoas
	    Use Spring HATEOAS library to allow adding HATEOAS links (Default: false)

	hideGenerationTimestamp
	    Hides the generation timestamp when files are generated. (Default: false)

	ignoreAnyOfInEnum
	    Ignore anyOf keyword in enum (Default: false)

	implicitHeaders
	    Skip header parameters in the generated API methods using @ApiImplicitParams annotation. (Default: false)

	implicitHeadersRegex
	    Skip header parameters that matches given regex in the generated API methods using @ApiImplicitParams annotation. Note: this parameter is ignored when implicitHeaders=true

	interfaceOnly
	    Whether to generate only API interface stubs without the server files. (Default: false)

	invokerPackage
	    root package for generated code (Default: org.openapitools.api)

	legacyDiscriminatorBehavior
	    Set to false for generators with better support for discriminators. (Python, Java, Go, PowerShell, C# have this enabled by default). (Default: true)
	        true - The mapping in the discriminator includes descendent schemas that allOf inherit from self and the discriminator mapping schemas in the OAS document.
	        false - The mapping in the discriminator includes any descendent schemas that allOf inherit from self, any oneOf schemas, any anyOf schemas, any x-discriminator-values, and the discriminator mapping schemas in the OAS document AND Codegen validates that oneOf and anyOf schemas contain the required discriminator and throws an error if the discriminator is missing.

	library
	    library template (sub-template) (Default: spring-boot)
	        spring-boot - Spring-boot Server application.
	        spring-cloud - Spring-Cloud-Feign client with Spring-Boot auto-configured settings.
	        spring-http-interface - Spring 6 HTTP interfaces (testing)

	licenseName
	    The name of the license (Default: Unlicense)

	licenseUrl
	    The URL of the license (Default: http://unlicense.org)

	modelPackage
	    package for generated models (Default: org.openapitools.model)

	openApiNullable
	    Enable OpenAPI Jackson Nullable library (Default: true)

	parentArtifactId
	    parent artifactId in generated pom N.B. parentGroupId, parentArtifactId and parentVersion must all be specified for any of them to take effect

	parentGroupId
	    parent groupId in generated pom N.B. parentGroupId, parentArtifactId and parentVersion must all be specified for any of them to take effect

	parentVersion
	    parent version in generated pom N.B. parentGroupId, parentArtifactId and parentVersion must all be specified for any of them to take effect

	performBeanValidation
	    Use Bean Validation Impl. to perform BeanValidation (Default: false)

	prependFormOrBodyParameters
	    Add form or body parameters to the beginning of the parameter list. (Default: false)

	reactive
	    wrap responses in Mono/Flux Reactor types (spring-boot only) (Default: false)

	requestMappingMode
	    Where to generate the class level @RequestMapping annotation. (Default: controller)
	        api_interface - Generate the @RequestMapping annotation on the generated Api Interface.
	        controller - Generate the @RequestMapping annotation on the generated Api Controller Implementation.
	        none - Do not add a class level @RequestMapping annotation.

	resourceFolder
	    resource folder for generated resources (Default: src/main/resources)

	responseWrapper
	    wrap the responses in given type (Future, Callable, CompletableFuture,ListenableFuture, DeferredResult, RxObservable, RxSingle or fully qualified type)

	returnSuccessCode
	    Generated server returns 2xx code (Default: false)

	scmConnection
	    SCM connection in generated pom.xml (Default: scm:git:git@github.com:openapitools/openapi-generator.git)

	scmDeveloperConnection
	    SCM developer connection in generated pom.xml (Default: scm:git:git@github.com:openapitools/openapi-generator.git)

	scmUrl
	    SCM URL in generated pom.xml (Default: https://github.com/openapitools/openapi-generator)

	serializableModel
	    boolean - toggle "implements Serializable" for generated models (Default: false)

	singleContentTypes
	    Whether to select only one produces/consumes content-type by operation. (Default: false)

	skipDefaultInterface
	    Whether to skip generation of default implementations for java8 interfaces (Default: false)

	snapshotVersion
	    Uses a SNAPSHOT version.
	        true - Use a SnapShot Version
	        false - Use a Release Version

	sortModelPropertiesByRequiredFlag
	    Sort model properties to place required parameters before optional parameters. (Default: true)

	sortParamsByRequiredFlag
	    Sort method arguments to place required parameters before optional parameters. (Default: true)

	sourceFolder
	    source folder for generated code (Default: src/main/java)

	testOutput
	    Set output folder for models and APIs tests (Default: ${project.build.directory}/generated-test-sources/openapi)

	title
	    server title name or client service name (Default: OpenAPI Spring)

	unhandledException
	    Declare operation methods to throw a generic exception and allow unhandled exceptions (useful for Spring `@ControllerAdvice` directives). (Default: false)

	useBeanValidation
	    Use BeanValidation API annotations (Default: true)

	useEnumCaseInsensitive
	    Use `equalsIgnoreCase` when String for enum comparison (Default: false)

	useFeignClientUrl
	    Whether to generate Feign client with url parameter. (Default: true)

	useJakartaEe
	    whether to use Jakarta EE namespace instead of javax (Default: false)

	useOneOfInterfaces
	    whether to use a java interface to describe a set of oneOf options, where each option is a class that implements the interface (Default: false)

	useOptional
	    Use Optional container for optional parameters (Default: false)

	useResponseEntity
	    Use the `ResponseEntity` type to wrap return values of generated API methods. If disabled, method are annotated using a `@ResponseStatus` annotation, which has the status of the first response declared in the Api definition (Default: true)

	useSpringBoot3
	    Generate code and provide dependencies for use with Spring Boot 3.x. (Use jakarta instead of javax in imports). Enabling this option will also enable `useJakartaEe`. (Default: false)

	useSpringController
	    Annotate the generated API as a Spring Controller (Default: false)

	useSwaggerUI
	    Open the OpenApi specification in swagger-ui. Will also import and configure needed dependencies (Default: true)

	useTags
	    use tags for creating interface and controller classnames (Default: false)

	virtualService
	    Generates the virtual service. For more details refer - https://github.com/virtualansoftware/virtualan/wiki (Default: false)

	withXml
	    whether to include support for application/xml content type and include XML annotations in the model (works with libraries that provide support for JSON and XML) (Default: false)

