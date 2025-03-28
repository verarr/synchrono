{
  description = "Java developent environment template";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs =
    {
      self,
      nixpkgs,
    }:
    let
      lib = nixpkgs.lib;
      pkgs = import nixpkgs {
        system = "x86_64-linux";
        config.allowUnfreePredicate =
          pkg:
          builtins.elem (lib.getName pkg) [
            # "vscode"
          ];
      };
    in
    {
      devShells."x86_64-linux".default = pkgs.mkShell {
        packages = with pkgs; [
          #########################
          # Java Development Kits #
          #########################

          # jdk # latest LTS
          # jdk8
          # jdk11
          # jdk17
          jdk21

          ###########
          # Editors #
          ###########

          # vscodium # https://wiki.nixos.org/wiki/VSCodium
          # vscode # https://wiki.nixos.org/wiki/Visual_Studio_Code
          jetbrains.idea-community-bin # add -bin to avoid bulding from source
          # jetbrains.idea-ultimate

          ###################
          # Editor features #
          ###################
          jdt-language-server
          python312
          #google-java-format
          llvmPackages_20.clang-tools
        ];

        #####################
        # Environment setup #
        #####################

        # nix-ld setup for Java VSCode extension
        # NIX_LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath [
        #   pkgs.stdenv.cc.cc
        #   pkgs.openssl
        # ];
        # NIX_LD = pkgs.lib.fileContents "${pkgs.stdenv.cc}/nix-support/dynamic-linker";

        shellHook = ''
          export LD_LIBRARY_PATH="''${LD_LIBRARY_PATH}''${LD_LIBRARY_PATH:+:}${pkgs.libglvnd}/lib"
        '';
      };
    };
}
