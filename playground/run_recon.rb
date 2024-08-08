#!/usr/bin/env ruby
require 'fileutils'
require 'date'

# Ensure the script is run from the "coatjava" directory
current_dir = File.basename(Dir.getwd)
if current_dir != 'coatjava'
  puts "Error: This script must be run from the 'coatjava' directory."
  exit 1
end

# Define paths and constants
playground_dir = './playground'
input_file = "#{playground_dir}/neutron_gun.hipo"
yaml_file = "#{playground_dir}/rga_fall_2018.yaml"
output_base_dir = "#{playground_dir}/output"

# Get the current date
today = Date.today
date_str = today.strftime("%m_%d_%Y")

# Determine the next iteration number
iterations_dir = "#{output_base_dir}/#{date_str}_"
iteration = 0

# Find the next available iteration number
while Dir.exist?("#{iterations_dir}#{"%04d" % iteration}")
  iteration += 1
end

# Format the output directory
output_dir = "#{iterations_dir}#{"%04d" % iteration}"
cooked_file = "#{output_dir}/cooked.hipo"

# Create the output directory
FileUtils.mkdir_p(output_dir)

# Run the command
command = "recon-util -i #{input_file} -o #{cooked_file} -y #{yaml_file}"
puts "Running command: #{command}"
system(command)

# Check if the command was successful
if $?.exitstatus == 0
  puts "Command executed successfully. Output written to #{cooked_file}"
else
  puts "Command failed with exit status: #{$?.exitstatus}"
end
