# This file controls the pinned version of nixpkgs we use for our Nix environment
{ config ? { android_sdk.accept_license = true; },
  pkgs ? (import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "70f0046b56b38ec462ebf6398dceea2fefe329bb";
    sha256 = "1ikz79hvh379sshrsmrmlipydc2pnvm3v4v7mii1k3hbc1p2c3r5";
    name = "nixpkgs-source";
  })) { inherit config; } }:

  {
    inherit pkgs config;
  }
