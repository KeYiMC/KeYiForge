package gg.m2ke4u.keyicore.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class WorkerConfig {
    private static final Logger logger = LogManager.getLogger();
    private static final File CONFIG_FILE = new File(GlobalConfig.CONFIG_DIR,"workers.yml");
    public static final YamlConfiguration CONFIGURATION = new YamlConfiguration();

    public static int ENTITIES_WORKER_THREADS;
    public static int GLOBAL_WORKER_THREADS;
    public static boolean AUTO_CLEAR_WORKERS;
    public static boolean SYNC_ENTITIES;
    public static boolean ASYNC_CATCHER_DISABLED;

    public static void init(){
        try{
            ConfigurationSection workerConfig;
            ConfigurationSection miscConfig;
            boolean needSave = false;
            if (!CONFIG_FILE.exists()){
                CONFIG_FILE.createNewFile();

                CONFIGURATION.load(CONFIG_FILE);
                CONFIGURATION.options().copyDefaults(true);

                workerConfig = CONFIGURATION.createSection("workers");
                miscConfig = CONFIGURATION.createSection("misc");

                logger.info("[KeYiForge]Worker config inited!");
                needSave = true;
            }else{
                CONFIGURATION.load(CONFIG_FILE);

                workerConfig = CONFIGURATION.getConfigurationSection("workers");
                miscConfig = CONFIGURATION.getConfigurationSection("misc");
            }

            ENTITIES_WORKER_THREADS = workerConfig.getInt("entities-worker-threads",Runtime.getRuntime().availableProcessors());
            GLOBAL_WORKER_THREADS = workerConfig.getInt("global-worker-threads",Runtime.getRuntime().availableProcessors());

            AUTO_CLEAR_WORKERS = miscConfig.getBoolean("auto-clear-workers",true);
            SYNC_ENTITIES = miscConfig.getBoolean("sync-entities-ticking",false);
            ASYNC_CATCHER_DISABLED = miscConfig.getBoolean("disable-async-catcher",false);

            if (needSave){
                CONFIGURATION.save(CONFIG_FILE);
            }
            logger.info("[KeYiForge]Worker config loaded");
        }catch (Exception e){
            logger.error("Failed to create or load config file!",e);
        }
        GlobalConfig.init();
    }
}
