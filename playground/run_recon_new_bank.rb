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
input_file = "/work/clas12/users/gmat/clas12/coatjava_dev/coatjava/playground/output/09_12_2024_0000/cooked_OC.hipo"
system("cp #{input_file} #{playground_dir}/OC.hipo")
system("rm #{playground_dir}/OC_filter.hipo")
system("hipo-utils -filter -b 'COAT::config,DC::tdc,ECAL::adc,ECAL::calib,ECAL::calib_OC,ECAL::clusters,ECAL::clusters_OC,ECAL::hits,ECAL::hits+,ECAL::moments,ECAL::moments_OC,ECAL::peaks,ECAL::tdc,FTOF::adc,FTOF::clusters,FTOF::hbclusters,FTOF::hbhits,FTOF::hits,FTOF::matchedclusters,FTOF::rawhits,FTOF::tdc,HTCC::adc,HTCC::rec,HTCC::tdc,HitBasedTrkg::Clusters,HitBasedTrkg::HBClusters,HitBasedTrkg::HBCrosses,HitBasedTrkg::HBHitTrkId,HitBasedTrkg::HBHits,HitBasedTrkg::HBSegments,HitBasedTrkg::HBTracks,HitBasedTrkg::Hits,HitBasedTrkg::Trajectory,MC::Event,MC::GenMatch,MC::Lund,MC::Particle,MC::RecMatch,MC::True,RASTER::adc,RASTER::position,REC::CaloExtras,REC::Cherenkov,REC::CovMat,REC::Event,REC::ScintExtras,REC::Scintillator,REC::Track,REC::Traj,RECHB::CaloExtras,RECHB::Cherenkov,RECHB::Event,RECHB::Particle,RECHB::ScintExtras,RECHB::Scintillator,RECHB::Track,RECHB::Traj,RUN::config,RUN::rf,TimeBasedTrkg::TBClusters,TimeBasedTrkg::TBCovMat,TimeBasedTrkg::TBCrosses,TimeBasedTrkg::TBHits,TimeBasedTrkg::TBSegments,TimeBasedTrkg::TBTracks,TimeBasedTrkg::Trajectory,ai::tracks' #{playground_dir}/OC.hipo -o #{playground_dir}/OC_filter.hipo")
# system("cp #{input_file} #{playground_dir}/OC_filter.hipo")
#system("hipo-utils -filter -b '*' #{playground_dir}/OC.hipo -o #{playground_dir}/OC_filter.hipo")

input_file = "#{playground_dir}/OC_filter.hipo"
yaml_file = "#{playground_dir}/rga_fall2018_OC.yaml"
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
command = "./playground/recon-util-new -i #{input_file} -o #{cooked_file} -y #{yaml_file} -n 5"
puts "Running command: #{command}"
system(command)

# Check if the command was successful
if $?.exitstatus == 0
  puts "Command executed successfully. Output written to #{cooked_file}"
else
  puts "Command failed with exit status: #{$?.exitstatus}"
end
