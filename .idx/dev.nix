{ pkgs, ... }: {
  channel = "stable-23.11"; 

  packages = [
    pkgs.jdk17
    pkgs.unzip
  ];

  env = {
    JAVA_HOME = "${pkgs.jdk17}/lib/openjdk";
  };

  idx = {
    extensions = [
      "mathiasfrohlich.Kotlin"
      "vscjava.vscode-java-pack"
    ];

    previews = {
      enable = true;
      previews = {
        android = {
          command = ["./gradlew" ":app:assembleDebug"];
          manager = "android";
        };
      };
    };
  };
}