# This file controls the pinned version of nixpkgs we use for our Nix environment
{ config ? { android_sdk.accept_license = true; },
  pkgs ? (import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "da364619549c337e6e8d9d45ddd2767a3d5cf574";
    sha256 = "1farmlpxp2v89ffqac5rdhbjps3gw5lhlmsmp1bgza4ggzq7rzfa";
    name = "nixpkgs-source";
  })) { inherit config; } }:

  {
    inherit pkgs config;
  }
