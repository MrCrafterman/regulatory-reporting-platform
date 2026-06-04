package dev.lgbonillar.regreporting.modules;

import dev.lgbonillar.regreporting.modules.demo.processor.DemoReportProcessor;
import dev.lgbonillar.regreporting.modules.demo.validation.DemoExcelValidator;
import dev.lgbonillar.regreporting.modules.global.processor.DefaultProcessorRegistry;
import dev.lgbonillar.regreporting.modules.global.processor.ProcessorRegistry;
import dev.lgbonillar.regreporting.modules.global.processor.ReportProcessor;
import dev.lgbonillar.regreporting.modules.global.validation.DefaultValidatorRegistry;
import dev.lgbonillar.regreporting.modules.global.validation.GenericValidator;
import dev.lgbonillar.regreporting.modules.global.validation.ValidatorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModulesConfiguration {

    @Bean
    public ValidatorRegistry validatorRegistry() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();

        GenericValidator demoValidator = new DemoExcelValidator();
        registry.register(demoValidator);

        return registry;
    }

    @Bean
    public ProcessorRegistry processorRegistry() {
        DefaultProcessorRegistry registry = new DefaultProcessorRegistry();

        ReportProcessor demoProcessor = new DemoReportProcessor();
        registry.register(demoProcessor);

        return registry;
    }
}