package com.example.demo.batchConfig;

import com.example.demo.model.SalesRecord;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public FlatFileItemReader<SalesRecord> reader() {
    FlatFileItemReader<SalesRecord> render = new FlatFileItemReader<>();
    render.setResource(new ClassPathResource("salesRecords1.csv"));
    render.setLineMapper(getLineMapper());
    render.setLinesToSkip(1);
    return render;
  }

  public LineMapper<SalesRecord> getLineMapper() {
    DefaultLineMapper<SalesRecord> lineMapper = new DefaultLineMapper<>();

    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();

    lineTokenizer.setNames(
      new String[] {
        "Country",
        "Item Type",
        "Order ID",
        "Units Sold",
        "Unit Price",
        "Unit Cost",
      }
    );

    lineTokenizer.setIncludedFields(new int[] { 1, 2, 6, 8, 9, 10 });

    BeanWrapperFieldSetMapper fieldSetMapper = new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(SalesRecord.class);

    lineMapper.setLineTokenizer(lineTokenizer);
    lineMapper.setFieldSetMapper(fieldSetMapper);

    return lineMapper;
  }

  @Bean
  public SalesProcessor processor() {
    return new SalesProcessor();
  }

  @Bean
  public JdbcBatchItemWriter<SalesRecord> writer() {
    JdbcBatchItemWriter<SalesRecord> writer = new JdbcBatchItemWriter<>();
    writer.setItemSqlParameterSourceProvider(
      new BeanPropertyItemSqlParameterSourceProvider<SalesRecord>()
    );
    writer.setSql(
      "insert into sales_records(country, item_type, order_id, units_sold, unit_price, unit_cost)" +
      " values(:country, :itemType, :orderId, :unitsSold, :unitPrice, :unitCost)"
    );
    writer.setDataSource(this.dataSource);

    return writer;
  }

  @Bean
  public Job importSalesProcessorJob() {
    return this.jobBuilderFactory.get("SALES-PROCESSING-IMPORT-JOB")
      .incrementer(new RunIdIncrementer())
      .flow(getStep1())
      .end()
      .build();
  }

  private Step getStep1() {
    return this.stepBuilderFactory.get("step1")
      .<SalesRecord, SalesRecord>chunk(10)
      .reader(reader())
      .processor(processor())
      .writer(writer())
      .build();
  }
}
