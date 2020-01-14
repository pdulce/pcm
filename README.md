# pcm
Uso de aplicaciones con Eclipse y Context Properties Modelling Framework.

Para engancharnos a proyectos subidos ya a GitHub remoto.

Requisitos. Eclipse + Plugin Git de Eclipse instalados en tu PC.

Primero. Seleccionar una carpeta en tu PC donde vas a clonar el repositorio.

Segundo. Configurar el acceso al repositorio remoto, indicando que en local lo vas a clonar sobre la carpeta del punto anterior.
En el asistente, si el repositorio tiene proyectos, selecciona la opción ‘Importar existing projects’. Es lo más habitual.
 
1. Comprobar que se ha generado desde la vista Git Repositories una carpeta Working Directory, con los proyectos que te has traído de GitHub.
2. Comprobar que se ha generado desde la vista Git Repositories una carpeta Remotes.

Tercero: Traer a local cambios del remoto. Para sincronizar desde lo que tienes en remoto, te sitúas en RemotesOrigin--> y pulsas Fetch.
	 
Cuarto: Llevar a remoto cambios locales. Para sincronizar tus cambios en local contra el repositorio remoto,  te sitúas en RemotesOrigin--> y pulsas Push.

Ambas opciones pueden realizarse desde la vista Java, o Resources, botón derecho sobre la carpeta o fuente, luego opción Team, y ahí las tres opciones primeras, Commit, Push y Fetch.

Quinto. Trabajando en modo standalone haciendo tus desarrollos y cambios, lo normal es ir haciendo tus commits contra el clonado en local del repositorio; recuerda, el commit no manda al destino remoto, sino a tu carpeta clonada en local. El motivo es que se evitan continuos trasiegos en red. 
Hasta que no queremos con seguridad (p.e. cuando hemos lanzado los tests de regresión en local) no deberíamos subir los cambios al repositorio remoto.

Si necesitas crear un nuevo proyecto en el repositorio remoto.

Primero. En el workbench de Eclipse, tienes que crear o tener ya creado tu proyecto Java o Web, o lo que sea.
Antes de subir cambios, comprueba que están bien las libraries y dependencias de otros proyectos del workbench, para subirlo todo completo a GitHub.
Ejecuta la aplicación y verifica que está todo listo para subir.

Para los proyectos Web, debes de tener instalados los plugins de Web development, J2EE, y también los JST Server Adapters.

Además, recuerda que si hay dependencia con el pcmFramework, debes indicarlo de manera explícita desde las Properties del proyecto, en la sección Deployment Assembly, no vale solo con referenciarlo como proyecto en el classpath para compilación.

También, si te da un problema al acceder en ejecución al classLoader de los servlets, añade en el jar de arranque del servidor, desde Configuración de lanzamientos (Runs configurations)--> Vía Acceso a Clases -- Entradas Usuario, el tools.jar de tu JDK.
 

Segundo.
Para añadir el nuevo proyecto a tu repositorio (queremos que aparezca en la carpeta Working Directory junto a los que ya existan en el repositorio remoto) has de pulsar en tu proyectoopciones botón derecho, la opción ‘Share project’, seleccionando el local repo clonado (carpeta C:\Users\99GU3997\git\pcm en nuestro ejemplo). 
Luego tienes que commitarlo para que se cargue en la carpeta Working Directory. OJO: aún no lo hemos llevado al repositorio remoto.

Para volcarlo al repositorio GitHub has de realizar la acción descrita en el punto Cuarto.

