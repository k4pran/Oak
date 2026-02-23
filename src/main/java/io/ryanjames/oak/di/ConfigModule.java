package io.ryanjames.oak.di;

import io.ryanjames.oak.config.ConfigProvider;
import io.ryanjames.oak.config.GlobalConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public final class ConfigModule {

    @Provides
    @Singleton
    static GlobalConfig provideGlobalConfig(ConfigProvider provider) {
        return new GlobalConfig(provider.outputDir(), provider.videoConfig(), provider.textConfig());
    }
}