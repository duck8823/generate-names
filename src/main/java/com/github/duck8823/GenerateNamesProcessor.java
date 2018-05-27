package com.github.duck8823;


import com.google.auto.common.MoreElements;
import com.squareup.javapoet.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.persistence.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * アノテーションプロセッサの実装
 * Created by maeda on 2016/01/09.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class GenerateNamesProcessor extends AbstractProcessor {

	private final List<String> CONSTRUCTOR_NAMES = Arrays.asList("<init>", "<clinit>");

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GenerateNames.class);
		for(final Element element : elements){
			final PackageElement packageElement = MoreElements.getPackage(element);
			final GenerateNames generateNames = element.getAnnotation(GenerateNames.class);
			final boolean isModel = element.getAnnotation(Entity.class) != null || element.getAnnotation(Embeddable.class) != null;
			TypeSpec.Builder builder = TypeSpec.classBuilder(element.getSimpleName().toString() + generateNames.suffix());
			AnnotationSpec generated = AnnotationSpec.builder(Generated.class)
													 .addMember("value", "$S", "GenerateNamesProcessor")
													 .build();

			builder.addAnnotation(generated)
				   .addModifiers(Modifier.PUBLIC)
				   .addJavadoc("Generated by generate-names.\n")
				   .addJavadoc("@see https://github.com/duck8823/generate-names\n");

			if(generateNames.createAsFields()) {
				builder.addFields(createFields(element, generateNames, isModel));
			}
			if(generateNames.createAsMethods()) {
				builder.addMethods(createMethods(element, generateNames));
			}
			final TypeSpec nameClass = builder.build();
			JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), nameClass).build();

			try {
				JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(element.toString() + generateNames.suffix());
				Writer writer = sourceFile.openWriter();
				javaFile.writeTo(writer);
				writer.close();
			} catch (IOException e){
				throw new RuntimeException(e);
			}
		}

		return true;
	}

	/**
	 * {@param element}内のフィールドを取得する
	 * @param element クラス要素
	 */
	private Set<FieldSpec> createFields(Element element, GenerateNames generateNames, boolean isModel) {
		return createFields(element, generateNames, isModel, new HashSet<>());
	}

	private Set<FieldSpec> createModelRelatedFields(Element element, HashSet<String> contains, String base) {
		Assert.notNull(element);
		Set<FieldSpec> fieldSpecs = new HashSet<>();
		final TypeElement typeElement = (TypeElement) element;
		for(Element enclosedElem : typeElement.getEnclosedElements()) {
			if(!enclosedElem.getKind().isField()){
				continue;
			}
			if(contains.contains(enclosedElem.toString())){
				continue;
			}
			String fieldName = enclosedElem.toString();
			fieldName = StringUtils.isEmpty(base) ? fieldName : base + "." + fieldName;
			String name = fieldName.replaceAll("\\.", "_").replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
			FieldSpec fieldSpec = FieldSpec.builder(String.class, name)
									.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
									.initializer("$S", fieldName)
									.addJavadoc("$Lのフィールド名\n", fieldName)
									.build();
			fieldSpecs.add(fieldSpec);

			if(!StringUtils.isEmpty(base) && typeElement.getAnnotation(Entity.class) != null){
				// 循環参照を防ぐため、2回目以降エンティティの場合は以降の要素は取得しない
				continue;
			}

			if (enclosedElem.getAnnotation(Embedded.class) != null ||
				enclosedElem.getAnnotation(EmbeddedId.class) != null ||
				enclosedElem.getAnnotation(ManyToOne.class) != null ||
				enclosedElem.getAnnotation(OneToOne.class) != null) {

				fieldSpecs.addAll(createModelRelatedFields(processingEnv.getTypeUtils().asElement(enclosedElem.asType()), contains, fieldName));
			} else if (enclosedElem.getAnnotation(ManyToMany.class) != null ||
					enclosedElem.getAnnotation(OneToMany.class) != null) {
				if (enclosedElem.asType() instanceof DeclaredType) {
					TypeMirror type = DeclaredType.class.cast(enclosedElem.asType()).getTypeArguments().get(0);
					fieldSpecs.addAll(createModelRelatedFields(processingEnv.getTypeUtils().asElement(type), contains, fieldName));
				}
			}
		}
		return fieldSpecs;
	}

	/**
	 * {@param element}内のフィールドを取得する
	 * @param element クラス要素
	 * @param contains 既に含まれる要素名のセット
	 */
	private Set<FieldSpec> createFields(Element element, GenerateNames generateNames, boolean isModel, HashSet<String> contains) {
		Set<FieldSpec> fieldSpecs = new HashSet<>();
		final TypeElement typeElement = (TypeElement) element;
		typeElement.getEnclosedElements().stream()
				.filter(o -> {
					if (o.getKind().isField()) return true;
					return generateNames.findMethods() &&
							o instanceof ExecutableElement &&
							((ExecutableElement) o).getParameters().isEmpty() &&
							!CONSTRUCTOR_NAMES.contains(o.getSimpleName().toString());
				})
				.filter(o -> !contains.contains(o.toString()))
				.forEach( o -> {
					String fieldName = o.getSimpleName().toString()
							.replaceAll("([a-z])([A-Z]+)", "$1_$2");
					String type = "フィールド";
					if (o instanceof ExecutableElement) {
						type = "メソッド";
					}
					FieldSpec fieldSpec = FieldSpec.builder(String.class, fieldName.toUpperCase())
							.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
							.initializer("$S", o.getSimpleName().toString())
							.addJavadoc("$Lの$L名\n", o.toString(), type)
							.build();
					fieldSpecs.add(fieldSpec);
				});

		if(isModel){
			fieldSpecs.addAll(createModelRelatedFields(element, contains, ""));
		}

		Element superclassElement = processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
		if(generateNames.findSuperclass() && superclassElement != null) {
			fieldSpecs.addAll(createFields(superclassElement, generateNames, isModel, contains));
		}
		return fieldSpecs;
	}

	/**
	 * {@param element}内のフィールドを取得する
	 * @param element クラス要素
	 */
	private Set<MethodSpec> createMethods(Element element, GenerateNames generateNames) {
		return createMethods(element, generateNames, new HashSet<>());
	}

	/**
	 * {@param element}内のフィールドを取得する
	 * @param element クラス要素
	 * @param contains 既に含まれる要素名のセット
	 */
	private Set<MethodSpec> createMethods(Element element, GenerateNames generateNames, HashSet<String> contains) {
		Set<MethodSpec> methodSpecs = new HashSet<>();
		final TypeElement typeElement = (TypeElement) element;
		typeElement.getEnclosedElements().stream()
				.filter(o -> {
					if (o.getKind().isField()) return true;
					return generateNames.findMethods() &&
							o instanceof ExecutableElement &&
							((ExecutableElement) o).getParameters().isEmpty() &&
							!CONSTRUCTOR_NAMES.contains(o.getSimpleName().toString());
				})
				.filter(o -> !contains.contains(o.toString()))
				.forEach(o -> {
					String methodName = o.getSimpleName().toString();
					String type = "フィールド";
					if (o instanceof ExecutableElement) {
						type = "メソッド";
					}
					MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
							.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
							.returns(String.class)
							.addCode("return $S;\n", o.getSimpleName().toString())
							.addJavadoc("$Lの$L名を取得します.\n@return $Lの$L名\n", o.toString(), type, o.toString(), type)
							.build();
					methodSpecs.add(methodSpec);
				});
		Element superclassElement = processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
		if(generateNames.findSuperclass() && superclassElement != null) {
			methodSpecs.addAll(createMethods(superclassElement, generateNames, contains));
		}
		return methodSpecs;
	}
}