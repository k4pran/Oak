package io.ryanjames.oak;

import io.ryanjames.oak.Document.DocumentGeneratorCoordinator;
import io.ryanjames.oak.config.ConfigProvider;
import dagger.BindsInstance;
import dagger.Component;
import io.ryanjames.oak.di.ConfigModule;
import io.ryanjames.oak.midi.MidiPipeline;

import javax.inject.Singleton;

@Singleton
@Component(modules = ConfigModule.class)
public interface AppComponent {
    VideoGeneratorCoordinator videoCoordinator();
    DocumentGeneratorCoordinator docCoordinator();
    MidiPipeline midiTransformerCoordinator();

    @Component.Builder
    interface Builder {
        @BindsInstance Builder configSource(ConfigProvider configProvider);
        AppComponent build();
    }
}

