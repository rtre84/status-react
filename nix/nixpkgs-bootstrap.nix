# This file controls the pinned version of nixpkgs we use for our Nix environment
{ config ? { android_sdk.accept_license = true; },
  pkgs ? (import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "c3018c2b9bbd744491efb8386f6112736f458001";
    # To get the sha256, execute: $ nix-prefetch-url --unpack https://github.com/status-im/nixpkgs/archive/$REV.tar.gz.
    # The last line will be the hash.
    sha256 = "0l1ly8hfwaad1alk0cs8axwcgrrsf2kd3ayga5zax3wfy3qrqbd8";
    name = "nixpkgs-source";
  })) { inherit config; } }:

  {
    inherit pkgs config;
  }
