package com.froobworld.farmcontrol.i18n;

import com.froobworld.farmcontrol.FarmControl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LanguageManager {
    private static final String DEFAULT_LANG = "en_us";
    private static final List<String> BUNDLED_LANGUAGES = List.of("en_us", "ru_ru", "zh_cn");
    
    private final FarmControl farmControl;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private String configuredDefaultLang = DEFAULT_LANG;

    public LanguageManager(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    public void load() {
        languages.clear();
        File languageDir = new File(farmControl.getDataFolder(), "language");
        if (!languageDir.exists()) {
            languageDir.mkdirs();
        }

        // Copy default language resources if missing
        for (String langCode : BUNDLED_LANGUAGES) {
            File targetFile = new File(languageDir, langCode + ".yml");
            if (!targetFile.exists()) {
                try (InputStream inputStream = farmControl.getResource("language/" + langCode + ".yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception e) {
                    farmControl.getLogger().warning("Could not save default language file: " + langCode + ".yml");
                }
            }
        }

        // Load all .yml files in language/
        File[] files = languageDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String langCode = file.getName().replace(".yml", "").toLowerCase(Locale.ROOT);
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                languages.put(langCode, config);
            }
        }

        // Update default configured language from plugin config if available
        try {
            configuredDefaultLang = farmControl.getFcConfig().language.get().toLowerCase(Locale.ROOT);
        } catch (Exception ignored) {
            configuredDefaultLang = DEFAULT_LANG;
        }
    }

    public String resolveLanguageCode(CommandSender sender) {
        if (sender instanceof Player player) {
            try {
                Locale playerLocale = player.locale();
                String code = playerLocale.toString().toLowerCase(Locale.ROOT).replace('-', '_');
                if (languages.containsKey(code)) {
                    return code;
                }
                // Fallback to language prefix (e.g. "zh" -> "zh_cn", "en" -> "en_us", "ru" -> "ru_ru")
                String langPrefix = playerLocale.getLanguage().toLowerCase(Locale.ROOT);
                for (String availableLang : languages.keySet()) {
                    if (availableLang.startsWith(langPrefix)) {
                        return availableLang;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return languages.containsKey(configuredDefaultLang) ? configuredDefaultLang : DEFAULT_LANG;
    }

    public Component getComponent(CommandSender sender, String key, TagResolver... tagResolvers) {
        String langCode = resolveLanguageCode(sender);
        String rawMessage = getRawMessage(langCode, key);
        if (rawMessage == null) {
            return Component.text("Missing translation key: " + key);
        }
        return MiniMessage.miniMessage().deserialize(rawMessage, tagResolvers);
    }

    public Component getConditionalSuffix(CommandSender sender, String key, int count, String placeholderName) {
        if (count <= 0) {
            return Component.empty();
        }
        String langCode = resolveLanguageCode(sender);
        String rawMessage = getRawMessage(langCode, key);
        if (rawMessage == null || rawMessage.isEmpty()) {
            return Component.empty();
        }
        return MiniMessage.miniMessage().deserialize(rawMessage, Placeholder.parsed(placeholderName, String.valueOf(count)));
    }

    public String getRawMessage(String langCode, String key) {
        YamlConfiguration config = languages.get(langCode);
        if (config != null && config.contains(key)) {
            return config.getString(key);
        }
        // Fallback to default configured language
        YamlConfiguration defaultConfig = languages.get(configuredDefaultLang);
        if (defaultConfig != null && defaultConfig.contains(key)) {
            return defaultConfig.getString(key);
        }
        // Fallback to hardcoded default en_us
        YamlConfiguration fallbackConfig = languages.get(DEFAULT_LANG);
        if (fallbackConfig != null && fallbackConfig.contains(key)) {
            return fallbackConfig.getString(key);
        }
        return null;
    }

    public void sendMessage(CommandSender sender, String key, TagResolver... tagResolvers) {
        Component component = getComponent(sender, key, tagResolvers);
        sender.sendMessage(component);
    }
}
