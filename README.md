# quick-carpet
Very small subset of carpet mod features that allows to very quickly update to new versions and snapshots

## commandPlayer
Enables /player command to spawn players  
* Type: `String`  
* Default value: `true`  
* Suggested options: `true`, `false`  
* Categories: `COMMAND`  
* Additional notes:  
  * It has an accompanying command  
  * Can be limited to 'ops' only, or a custom permission level  
  
## commandSpawn
Enables /spawn command for mobcaps information  
* Type: `String`  
* Default value: `true`  
* Suggested options: `true`, `false`  
* Categories: `COMMAND`  
* Additional notes:  
  * It has an accompanying command  
  * Can be limited to 'ops' only, or a custom permission level  
  
## commandTick
Enables /tick command to control game clocks  
Available functions: warp, rate and health  
* Type: `String`  
* Default value: `true`  
* Suggested options: `true`, `false`  
* Categories: `COMMAND`  
* Additional notes:  
  * It has an accompanying command  
  * Can be limited to 'ops' only, or a custom permission level  
  
## explosionNoBlockDamage
Explosions won't destroy blocks  
* Type: `boolean`  
* Default value: `false`  
* Required options: `true`, `false`  
* Categories: `CREATIVE`  
  
## fillLimit
Customizable fill/clone volume limit  
* Type: `int`  
* Default value: `32768`  
* Suggested options: `32768`, `250000`, `1000000`  
* Categories: `CREATIVE`  
* Additional notes:  
  * You must choose a value from 1 to 20M  
  
## fillUpdates
fill/clone/setblock and structure blocks cause block updates  
* Type: `boolean`  
* Default value: `true`  
* Required options: `true`, `false`  
* Categories: `CREATIVE`  
  
## hopperCounters
hoppers pointing to wool will count items passing through them  
Enables /counter command, and actions while placing red and green carpets on wool blocks  
Use /counter <color?> reset to reset the counter, and /counter <color?> to query  
Counters are global and shared between players, 16 channels available  
Items counted are destroyed, count up to one stack per tick per hopper  
* Type: `boolean`  
* Default value: `false`  
* Required options: `true`, `false`  
* Categories: `COMMAND`, `CREATIVE`  
* Additional notes:  
  * It has an accompanying command  
  
## interactionUpdates
placing blocks cause block updates  
* Type: `boolean`  
* Default value: `true`  
* Required options: `true`, `false`  
* Categories: `CREATIVE`  
  
## smoothClientAnimations
smooth client animations with low tps settings  
works only in SP, and will slow down players  
* Type: `boolean`  
* Default value: `false`  
* Required options: `true`, `false`  
* Categories: `CREATIVE`  
  
## superSecretSetting
Gbhs sgnf sadsgras fhskdpri!  
* Type: `boolean`  
* Default value: `false`  
* Required options: `true`, `false`  
* Categories: `CREATIVE`  
  