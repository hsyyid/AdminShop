package io.github.hsyyid.adminshop.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.hsyyid.adminshop.AdminShop;
import org.spongepowered.api.world.Location;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigManager
{
	private static Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Location.class, new LocationAdapter()).create();
	
	public static void readAdminShops()
	{
		String json = null;

		try
		{
			json = readFile("AdminShops.json", StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			System.out.println("Could not read JSON file!");
		}

		if (json != null)
		{
			AdminShop.adminShops = new ArrayList<AdminShopObject>(Arrays.asList(gson.fromJson(json, AdminShopObject[].class)));
		}
		else
		{
			System.out.println("Could not read GSON from JSON file!");
		}
	}
	
	public static void readBuyAdminShops()
	{
		String json = null;
		
		try
		{
			json = readFile("BuyAdminShops.json", StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			System.out.println("Could not read JSON file!");
		}

		if (json != null)
		{
			AdminShop.buyAdminShops = new ArrayList<AdminShopObject>(Arrays.asList(gson.fromJson(json, AdminShopObject[].class)));
		}
		else
		{
			System.out.println("Could not read JSON file!");
		}
	}
	
	public static void writeAdminShops()
	{
		String json = gson.toJson(AdminShop.adminShops);
		
		try
		{
			FileWriter fileWriter = new FileWriter("AdminShops.json");
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(json);
			bufferedWriter.flush();
			bufferedWriter.close();
		}
		catch (IOException ex)
		{
			System.out.println("Could not save JSON file!");
		}
	}
	
	public static void writeBuyAdminShops()
	{
		String json = gson.toJson(AdminShop.buyAdminShops);
		
		try
		{
			FileWriter fileWriter = new FileWriter("BuyAdminShops.json");
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(json);
			bufferedWriter.flush();
			bufferedWriter.close();
		}
		catch (IOException ex)
		{
			System.out.println("Could not save JSON file!");
		}
	}
	
	private static String readFile(String path, Charset encoding) throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
