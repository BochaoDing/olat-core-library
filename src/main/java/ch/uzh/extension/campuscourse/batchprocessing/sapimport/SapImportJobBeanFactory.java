package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.batchprocessing.BatchJobBlankLineRecordSeparatorPolicy;
import ch.uzh.extension.campuscourse.data.dao.OrgDao;
import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import ch.uzh.extension.campuscourse.data.entity.Org;
import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.model.*;
import org.olat.core.commons.persistence.DBImpl;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Schraner
 */
@Configuration
public class SapImportJobBeanFactory {

	@Autowired
	private DBImpl dbInstance;

	@Autowired
	private OrgDao orgDao;

	@Bean
	@Scope("step")
	public FlatFileItemReader<Student> studentReader(@Value("#{jobParameters['studentResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{0, 1, 2, 3, 4};
		String[] attributeNamesOfTargetClass = new String []{"id", "registrationNr", "firstName", "lastName", "email"};
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, Student.class);
	}

	@Bean
	@Scope("step")
	public FlatFileItemReader<Lecturer> lecturerReader(@Value("#{jobParameters['lecturerResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{0, 1, 2, 3, 4, 5};
		String[] attributeNamesOfTargetClass = new String []{"personalNr", "firstName", "lastName", "privateEmail", "email", "additionalPersonalNrs"};
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, Lecturer.class);
	}

	@Bean
	@Scope("step")
	public FlatFileItemReader<CourseSemesterOrgId> courseReader(@Value("#{jobParameters['courseResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
		String[] attributeNamesOfTargetClass = new String []{"id", "lvKuerzel", "title", "lvNr", "language", "category",
				"eLearningSupported", "startDate", "endDate", "vzzLink", "semester", "exclude", "org1", "org2", "org3",
				"org4", "org5", "org6", "org7", "org8", "org9"};
		String dateFormat = "dd.MM.yyyy";
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, dateFormat, CourseSemesterOrgId.class);
	}

	@Bean
	@Scope("step")
	public FlatFileItemReader<StudentIdCourseId> studentCourseReader(@Value("#{jobParameters['studentCourseResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{1, 3};
		String[] attributeNamesOfTargetClass = new String []{"courseId", "studentId"};
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, StudentIdCourseId.class);
	}

	@Bean
	@Scope("step")
	public FlatFileItemReader<LecturerIdCourseId> lecturerCourseReader(@Value("#{jobParameters['lecturerCourseResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{0, 1};
		String[] attributeNamesOfTargetClass = new String []{"courseId", "lecturerId"};
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, LecturerIdCourseId.class);
	}

	@Bean
	@Scope("step")
	public FlatFileItemReader<TextCourseId> textReader(@Value("#{jobParameters['textResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{0, 1, 2, 3, 4};
		String[] attributeNamesOfTargetClass = new String []{"courseId", "textTypeId", "textTypeName", "lineNumber", "line"};
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, TextCourseId.class);
	}

	@Bean
	@Scope("step")
	public FlatFileItemReader<EventCourseId> eventReader(@Value("#{jobParameters['eventResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{0, 1, 2, 3};
		String[] attributeNamesOfTargetClass = new String []{"courseId", "date", "start", "end"};
		String dateFormat = "dd.MM.yyyy";
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, dateFormat, EventCourseId.class);
	}

	@Bean
	@Scope("step")
	public FlatFileItemReader<Org> orgReader(@Value("#{jobParameters['orgResource']}") String pathToInputFile) {
		int[] includedColumns = new int[]{0, 10, 12};
		String[] attributeNamesOfTargetClass = new String []{"id", "shortName", "name"};
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, Org.class);
	}

	@Bean
	@Scope("step")
	public SapImportWriter<Org> orgWriter() {
		return new SapImportWriter<>(dbInstance, orgDao);
	}

	private static <T> FlatFileItemReader<T> createFlatFileItemReader(String pathToInputFile,
															   int[] includedColumns,
															   String[] attributeNamesOfTargetClass,
															   Class<T> targetClass) {
		return createFlatFileItemReader(pathToInputFile, includedColumns, attributeNamesOfTargetClass, null, targetClass);
	}

	private static <T> FlatFileItemReader<T> createFlatFileItemReader(String pathToInputFile,
															   int[] includedColumns,
															   String[] attributeNamesOfTargetClass,
															   String dateFormat,
															   Class<T> targetClass) {

		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		delimitedLineTokenizer.setStrict(true);
		delimitedLineTokenizer.setDelimiter(";");
		delimitedLineTokenizer.setQuoteCharacter('☠');
		if (includedColumns != null) {
			delimitedLineTokenizer.setIncludedFields(includedColumns);
		}
		delimitedLineTokenizer.setNames(attributeNamesOfTargetClass);

		BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
		beanWrapperFieldSetMapper.setStrict(true);
		beanWrapperFieldSetMapper.setTargetType(targetClass);
		if (dateFormat != null) {
			Map<String, PropertyEditorSupport> customEditors = new HashMap<>();
			customEditors.put("java.util.Date", new CustomDateEditor(new SimpleDateFormat(dateFormat), true));
			beanWrapperFieldSetMapper.setCustomEditors(customEditors);
		}

		DefaultLineMapper<T> defaultLineMapper = new DefaultLineMapper<>();
		defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
		defaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

		FlatFileItemReader<T> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setStrict(false);
		flatFileItemReader.setResource(new FileSystemResource(pathToInputFile));
		flatFileItemReader.setEncoding("UTF-8");
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setLineMapper(defaultLineMapper);
		flatFileItemReader.setRecordSeparatorPolicy(new BatchJobBlankLineRecordSeparatorPolicy());

		return flatFileItemReader;
	}

}
